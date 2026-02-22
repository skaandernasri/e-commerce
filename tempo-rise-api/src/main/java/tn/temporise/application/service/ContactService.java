package tn.temporise.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.auth.UnauthorizedException;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.ContactMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.*;
import tn.temporise.infrastructure.security.utils.CookiesUtil;


@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {
  private final ExceptionFactory exceptionFactory;
  private final ContactRepo contactRepo;
  private final ContactMapper contactMapper;
  private final UserRepo userRepo;
  private final CommandeRepo commandeRepo;
  private final EmailInterface emailInterface;
  private final NotifService notifService;
  private final UtilisateurAnonymeService utilisateurAnonymeService;
  private final CookiesUtil cookiesUtil;

  @Transactional
  public ContactDto createContact(ContactCreateRequestDto createRequestDto) {
    try {
      if (createRequestDto == null || createRequestDto.getType() == null
          || createRequestDto.getMessage() == null)
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      if (createRequestDto.getUserId() == null) {
        if (createRequestDto.getEmail() == null)
          throw exceptionFactory.badRequest("badrequest.invalid_input");
        UtilisateurAnonymeResponse utilisateurAnonymeResponse = utilisateurAnonymeService
            .getOrCreateUtilisateurAnonyme(cookiesUtil.getUUIDCookieValue("anonyme_session_token"));
        createRequestDto.setUserId(utilisateurAnonymeResponse.getId());
      } else {
        UtilisateurModel user = userRepo.findById(createRequestDto.getUserId());
        if (createRequestDto.getEmail() == null)
          createRequestDto.setEmail(user.email());
      }
      Contact contact = contactMapper.toContact(createRequestDto);
      if (createRequestDto.getType() == TypeContact.RETOUR) {
        if (createRequestDto.getCommandeId() == null)
          throw exceptionFactory.badRequest("badrequest.invalid_input");
        else {
          // if (!commandeService.isCommandeOwnerByOrderId(createRequestDto.getCommandeId())) {
          // log.info("User not owner of commande");
          // throw exceptionFactory.unauthorized("unauthorized.access_denied");
          // }
          if (!commandeRepo.existsByIdAndUserId(createRequestDto.getCommandeId(),
              createRequestDto.getUserId()))
            throw exceptionFactory.notFound("notfound.commande");
          contact = contact.toBuilder()
              .commande(commandeRepo.findById(createRequestDto.getCommandeId())).build();
        }
      } else {
        contact = contact.toBuilder().commande(null).build();
      }
      contact = contact.toBuilder().statusContact(StatusContact.EN_COURS).build();
      return contactMapper.toContactDto(contactRepo.save(contact));
    } catch (BadRequestException | NotFoundException | UnauthorizedException e) {
      log.warn("Contact creation failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Contact creation failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  @Transactional
  public void updateContact(Long id, ContactStatusUpdateDto updateRequestDto) {
    try {
      if (id == null || updateRequestDto == null || updateRequestDto.getStatusContact() == null)
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      Contact contact = contactRepo.findById(id);
      if (contact.statusContact().equals(StatusContact.RESOLU))
        throw exceptionFactory.badRequest("badrequest.probleme_resolu");
      if (contact.type().equals(TypeContact.AIDE)) {
        contact = contact.toBuilder().statusContact(updateRequestDto.getStatusContact()).build();
        contactRepo.save(contact);
        emailInterface.sendContactAideResponseEmail(contact.user().email(),
            contact.user().nom() + " " + contact.user().prenom(), contact.subject(),
            contact.message(), updateRequestDto.getAdminResponse());
      } else {
        if (updateRequestDto.getIsRefunded() == null || updateRequestDto.getRefundMethod() == null)
          throw exceptionFactory.badRequest("badrequest.invalid_input");
        Commande commande = commandeRepo.findById(contact.commande().id());
        Paiement paiement = commande.paiements().stream()
            .filter(p -> p.status().equals(WebhookResponsePayment.StatusEnum.COMPLETED)).findFirst()
            .orElseThrow(() -> exceptionFactory.notFound("notfound.paiement"));
        if (updateRequestDto.getIsRefunded()) {
          // Paiement paiement = commande.paiements().stream()
          // .filter(p -> p.status().equals(WebhookResponsePayment.StatusEnum.EXPIRED) ||
          // p.status().equals(WebhookResponsePayment.StatusEnum.PENDING))
          // .findFirst().orElseThrow(() -> exceptionFactory.notFound("notfound.paiement"));
          // send externe api for refund here that will return the refund link and i do the code
          // below in the webhook
          // Contact finalContact = contact;
          // commande.lignesCommande().forEach(ligneCommande -> {
          // RetourProduit retourProduit = RetourProduit.builder().variant(ligneCommande.variant())
          // .raisonRetour(finalContact.message()).utilisateur(finalContact.user()).build();
          // retourProduitRepo.save(retourProduit);
          // Variant variant = variantRepo.findById(ligneCommande.variant().id());
          // variantRepo.save(variant.toBuilder()
          // .quantity(variant.quantity() + ligneCommande.quantite()).build());
          // });
          notifService.notifyRemboursement(contact);
          emailInterface.sendRefundNotificationEmail(paiement, updateRequestDto.getAdminResponse());
        } else {
          // Paiement paiement = commande.paiements().stream()
          // .filter(p -> p.status().equals(WebhookResponsePayment.StatusEnum.EXPIRED) ||
          // p.status().equals(WebhookResponsePayment.StatusEnum.PENDING))
          // .findFirst().orElseThrow(() -> exceptionFactory.notFound("notfound.paiement"));
          emailInterface.sendRefundRejectedNotificationEmail(paiement,
              updateRequestDto.getAdminResponse());
        }
        contact = contact.toBuilder().isRefunded(updateRequestDto.getIsRefunded())
            .statusContact(updateRequestDto.getStatusContact())
            .refundMethod(updateRequestDto.getRefundMethod()).build();
        contactRepo.save(contact);
      }
    } catch (BadRequestException | NotFoundException e) {
      log.warn("Contact update failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Contact update failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public GetContacts200Response getAllContacts(int page, int size, TypeContact type,
      StatusContact statusContact, RefundMethod refundMethod, SortEnum sort) {
    {
      page = Math.max(0, page - 1);
      if (type != null && type.equals(TypeContact.AIDE))
        refundMethod = null;
      ContactFilter filter = ContactFilter.builder().type(type).statusContact(statusContact)
          .refundMethod(refundMethod).build();
      Page<Contact> contactPage = contactRepo.findAll(filter,
          PageRequest.of(page, size,
              (sort != null && sort.equals(SortEnum.ASC)) ? Sort.by("createdAt").ascending()
                  : Sort.by("createdAt").descending()));
      log.info("Retrieved all contacts {}", contactPage.getContent());
      GetContacts200Response response = new GetContacts200Response();
      response
          .setContent(contactPage.getContent().stream().map(contactMapper::toContactDto).toList());
      response.setTotalElements(contactPage.getTotalElements());
      response.setTotalPages(contactPage.getTotalPages());
      response.setCurrentPage(contactPage.getNumber());
      response.setSize(contactPage.getSize());
      return response;
    }

  }

}
