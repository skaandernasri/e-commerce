package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.LigneCommandeService;
import tn.temporise.domain.model.CommandeResponse;
import tn.temporise.domain.model.LigneCommandeRequest;
import tn.temporise.domain.model.Response;
import tn.temporise.infrastructure.api.LigneCommandeApi;

@RestController
@Slf4j
@RequiredArgsConstructor
public class LigneCommandeController implements LigneCommandeApi {
  private final LigneCommandeService ligneCommandeService;

  @Override
  public ResponseEntity<Response> _deleteLigneCommande(Long commandeId, Long variantId)
      throws Exception {
    return ResponseEntity.ok(ligneCommandeService.deleteLigneCommande(commandeId, variantId));
  }

  @Override
  public ResponseEntity<CommandeResponse> _updateLigneCommande(Long commandeId,
      LigneCommandeRequest ligneCommandeRequest) throws Exception {
    return ResponseEntity
        .ok(ligneCommandeService.updateLigneCommande(commandeId, ligneCommandeRequest));
  }
}
