package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.FactureService;
import tn.temporise.domain.model.FactureRequest;
import tn.temporise.domain.model.FactureResponse;
import tn.temporise.domain.model.Response;
import tn.temporise.infrastructure.api.FacturesApi;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class FactureController implements FacturesApi {
  private final FactureService factureService;

  @Override
  public ResponseEntity<FactureResponse> _createFacture(FactureRequest factureRequest)
      throws Exception {
    return ResponseEntity.ok(factureService.saveFacture(factureRequest));
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<Response> _deleteAllFactures() throws Exception {
    factureService.deleteAllFactures();
    Response response = new Response();
    response.setCode("200");
    response.setMessage("Toutes les factures ont été supprimées avec succès");
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<Response> _deleteFacture(Long id) throws Exception {
    factureService.deleteFacture(id);
    Response response = new Response();
    response.setCode("200");
    response.setMessage("Facture supprimée avec succès");
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<List<FactureResponse>> _getAllFactures() throws Exception {
    return ResponseEntity.ok(factureService.getAllFactures());
  }

  @Override
  public ResponseEntity<FactureResponse> _getFactureByCommandeId(Long id) throws Exception {
    return ResponseEntity.ok(factureService.getFacturesByCommandeId(id));
  }

  @Override
  public ResponseEntity<FactureResponse> _getFactureById(Long id) throws Exception {
    return ResponseEntity.ok(factureService.getFactureById(id));
  }

  @Override
  public ResponseEntity<List<FactureResponse>> _getFactureByUserId(Long id) throws Exception {
    return ResponseEntity.ok(factureService.getFacturesByUserId(id));
  }
}
