package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.CommandeService;
import tn.temporise.application.service.MergeUserService;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.api.CommandesApi;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CommandeController implements CommandesApi {
  private final CommandeService commandeService;
  private final MergeUserService mergeUserService;

  @PreAuthorize("@commandeService.isConnectedUserCommandeOwner(#commande.utilisateurId)")
  @Override
  public ResponseEntity<PaiementResponse> _createCommande(CommandeRequest commande)
      throws Exception {
    PaiementResponse response = commandeService.createCommande(commande);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<PaiementResponse> _createCommandeAdmin(
      CommandeAdminRequest commandeAdminRequest) throws Exception {
    PaiementResponse response = commandeService.createCommandeAdmin(commandeAdminRequest);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Override
  public ResponseEntity<PaiementResponse> _createCommandeGuest(
      CommandeGuestRequest commandeGuestRequest) throws Exception {
    PaiementResponse response = commandeService.createCommandeAsGuest(commandeGuestRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }


  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<Response> _deleteAllCommandes() throws Exception {
    commandeService.deleteAllCommandes();
    Response response = new Response();
    response.setCode("200");
    response.setMessage("Commandes supprimées avec succès");
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<Response> _deleteCommande(Long id) throws Exception {
    commandeService.deleteCommande(id);
    Response response = new Response();
    response.setCode("200");
    response.setMessage("Commande supprimée avec succès");
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<List<CommandeResponse>> _getAllCommandes() throws Exception {
    return ResponseEntity.ok(commandeService.getAllCommandes());
  }

  @PreAuthorize("@commandeService.isAdmin(#userId) or @commandeService.isCommandeOwner(#id,#userId)")
  @Override
  public ResponseEntity<CommandeResponse> _getCommandeById(Long id, Long userId) throws Exception {
    return ResponseEntity.ok(commandeService.getCommandeById(id));
  }

  @PreAuthorize("@commandeService.isAdmin(#userId) or @commandeService.isCommandeOwnerByRef(#paymentRef, #userId)")
  @Override
  public ResponseEntity<CommandeResponse> _getCommandeByRef(String paymentRef, Long userId)
      throws Exception {
    return ResponseEntity.ok(commandeService.getCommandeByPaymentRef(paymentRef));
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN') or @commandeService.isConnectedUserCommandeOwner(#id)")
  @Override
  public ResponseEntity<List<CommandeResponse>> _getCommandeByUserId(Long id) throws Exception {
    return ResponseEntity.ok(commandeService.getCommandesByUtilisateurId(id));
  }

  @Override
  public ResponseEntity<GetFilteredCommandesPage200Response> _getFilteredCommandesPage(Integer page,
      Integer size, StatusCommandeEnum status, String email, Boolean orderByCreationDateDesc)
      throws Exception {
    return ResponseEntity.ok(commandeService.getFilteredCommandesPage(page, size, status, email,
        orderByCreationDateDesc));
  }

  @Override
  public ResponseEntity<Response> _mergeCommande(MergeCommandeRequest mergeCommandeRequest)
      throws Exception {
    return ResponseEntity.ok(mergeUserService.mergeUserCommande(mergeCommandeRequest.getIdUser(),
        mergeCommandeRequest.getIdUserAnonyme()));
  }

  @Override
  public ResponseEntity<CommandeResponse> _updateCommande(Long id,
      UpdateCommandeRequest updateCommandeRequest) throws Exception {
    return null;
  }

  @Override
  public ResponseEntity<CommandeResponse> _updateCommandeCustomerDetails(Long id,
      UpdateCommandeCustomerDetailsRequest updateCommandeCustomerDetailsRequest) throws Exception {
    return ResponseEntity
        .ok(commandeService.updateCustomerDetails(id, updateCommandeCustomerDetailsRequest));
  }


  @Override
  public ResponseEntity<CommandeResponse> _updateCommandeStatus(Long id,
      UpdateCommandeStatusRequest updateCommandeRequest) throws Exception {
    return ResponseEntity.ok(commandeService.updateStatusCommande(id, updateCommandeRequest));
  }
}
