package tn.temporise.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.NotifMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.NotifRepo;
import tn.temporise.domain.port.UserNotifRepo;
import tn.temporise.domain.port.UserRepo;
import tn.temporise.infrastructure.persistence.entity.UserNotifId;

import java.time.ZoneOffset;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotifService {
  private final NotifRepo notifRepo;
  private final NotifMapper notifMapper;
  private final ExceptionFactory exceptionFactory;
  private final SimpMessagingTemplate simpMessagingTemplate;
  private final UserNotifRepo userNotifRepo;
  private final UserRepo userRepo;

  @Transactional
  public void notifyOrder(Commande commande) {
    try {
      if (commande == null || commande.user() == null || commande.user().id() == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }
      log.info("commande in notif order {}", commande);
      UtilisateurModel commandOwner = userRepo.findById(commande.user().id());
      // Create and save the notification
      Notif generalNotif = notifRepo.save(Notif
          .builder().title("New Order").message("a new order has been created with reference "
              + commande.id() + " for the user " + commandOwner.email())
          .redirectUrl("/orders").type(NotifType.NEW_ORDER).build());
      Notif specificNotif = notifRepo.save(Notif.builder().title("New Order")
          .message("Your order " + "#" + commande.id() + " is passed").redirectUrl("/orders")
          .type(NotifType.NEW_ORDER).build());
      // Get all admins
      List<UtilisateurModel> admins = userRepo.findByRole(Role.ADMIN);
      log.info("all admin users {}", admins);
      Notif notif = null;
      // Notify all admins privately
      for (UtilisateurModel admin : admins) {
        if (commandOwner.id().equals(admin.id())) {
          notif = specificNotif;
          userNotifRepo.save(UserNotif.builder().id(new UserNotifId(admin.id(), notif.id()))
              .notif(notif).user(commandOwner).build());
        } else {
          notif = generalNotif;
          userNotifRepo.save(UserNotif.builder().id(new UserNotifId(admin.id(), notif.id()))
              .notif(notif).user(admin).build());
        }
        Long unreadCount = userNotifRepo.countByUserIdAndIsRead(admin.id(), false);

        NotificationResponse notifResponse = notifMapper.modelToResponse(notif);
        notifResponse.setUnreadCount(unreadCount);
        notifResponse.setIsRead(false);

        simpMessagingTemplate.convertAndSendToUser(admin.email(), "/queue", notifResponse);
      }

      // Notify the user who made the order if not admin
      boolean isUserAdmin = admins.stream().anyMatch(admin -> admin.id().equals(commandOwner.id()));
      if (!isUserAdmin) {
        userNotifRepo
            .save(UserNotif.builder().id(new UserNotifId(commandOwner.id(), specificNotif.id()))
                .notif(specificNotif).user(commandOwner).build());
        Long unreadCount = userNotifRepo.countByUserIdAndIsRead(commandOwner.id(), false);

        NotificationResponse notifResponse = notifMapper.modelToResponse(specificNotif);
        notifResponse.setUnreadCount(unreadCount);
        notifResponse.setIsRead(false);

        simpMessagingTemplate.convertAndSendToUser(commandOwner.email(), "/queue", notifResponse);

      }

      log.info("Notification sent to all admins and the ordering user");

    } catch (BadRequestException | NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  @Transactional
  public void notifyPromotion(Promotion promotion) {
    try {
      if (promotion == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }

      // Create and save the notification
      Notif notif = notifRepo.save(Notif.builder().title("New Promotion")
          .message("there is a new promotion available for our product " + promotion.produit().nom()
              + " with a reduction up to " + promotion.reduction() + "%")
          .redirectUrl("/product/" + promotion.produit().id()).type(NotifType.PROMOTION).build());
      log.info("Created notif: {}", notif);

      // Notify all users
      List<UtilisateurModel> users = userRepo.findAll();
      for (UtilisateurModel user : users) {
        userNotifRepo.save(UserNotif.builder().id(new UserNotifId(user.id(), notif.id()))
            .notif(notif).user(user).build());

        Long unreadCount = userNotifRepo.countByUserIdAndIsRead(user.id(), false);

        NotificationResponse notifResponse = notifMapper.modelToResponse(notif);
        notifResponse.setUnreadCount(unreadCount);
        notifResponse.setIsRead(false);

        simpMessagingTemplate.convertAndSendToUser(user.email(), "/queue", notifResponse);
      }

      log.info("Notification sent to all users");



    } catch (BadRequestException | NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public void notifyRemboursement(Contact contact) {
    try {
      if (contact == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }
      log.info("contact: {}", contact);
      Notif notif = notifRepo
          .save(Notif.builder().title("New Remboursement").message("your order has been rembursed")
              .type(NotifType.REMBOURSEMENT).redirectUrl("/reboursementUrl/").build());
      log.info("Created notif: {}", notif);

      // Notify user
      userNotifRepo.save(UserNotif.builder().id(new UserNotifId(contact.user().id(), notif.id()))
          .notif(notif).user(contact.user()).build());
      Long unreadCount = userNotifRepo.countByUserIdAndIsRead(contact.user().id(), false);

      NotificationResponse notifResponse = notifMapper.modelToResponse(notif);
      notifResponse.setUnreadCount(unreadCount);
      notifResponse.setIsRead(false);

      simpMessagingTemplate.convertAndSendToUser(contact.user().email(), "/queue", notifResponse);

      log.info("Notification sent to all users");
    } catch (BadRequestException | NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public void notifyCodePromo(UtilisateurModel user, CodePromo codePromo) {
    try {

      log.info("codePromo: {}", codePromo);
      Notif notif = notifRepo.save(Notif.builder().title("New Code Promo")
          .message("Thanks for your loyalty here a promoCode : " + codePromo.code()
              + " with a reduction up to " + codePromo.reduction() + "%" + " valid until "
              + codePromo.dateExpiration().atOffset(ZoneOffset.UTC))
          .type(NotifType.CODE_PROMO).redirectUrl("/codePromoUrl/").build());
      log.info("Created notif: {}", notif);

      // Notify user
      userNotifRepo.save(UserNotif.builder().id(new UserNotifId(user.id(), notif.id())).notif(notif)
          .user(user).build());
      Long unreadCount = userNotifRepo.countByUserIdAndIsRead(user.id(), false);

      NotificationResponse notifResponse = notifMapper.modelToResponse(notif);
      notifResponse.setUnreadCount(unreadCount);
      notifResponse.setIsRead(false);

      simpMessagingTemplate.convertAndSendToUser(user.email(), "/queue", notifResponse);

      log.info("Notification sent to all users");
    } catch (BadRequestException | NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

}
