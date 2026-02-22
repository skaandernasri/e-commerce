package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.AvisService;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.api.AvisApi;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class AvisController implements AvisApi {
  private final AvisService avisService;

  @Override
  public ResponseEntity<AvisResponse> _createAvis(AvisRequest avisRequest) throws Exception {
    AvisResponse avis = avisService.createAvis(avisRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(avis);
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<Response> _deleteAllAvis() throws Exception {
    avisService.deleteAllAvis();
    Response response = new Response();
    response.setCode("200");
    response.setMessage("Tous les avis ont été supprimés avec succès");
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Response> _deleteAvis(Long id) throws Exception {
    log.info("id---: " + id);
    avisService.deleteAvis(id);
    Response responseBody = new Response();
    responseBody.setCode("200");
    responseBody.setMessage("Avis supprimé !");
    return ResponseEntity.ok(responseBody);
  }

  @Override
  public ResponseEntity<List<AvisResponse>> _getAllAvis() throws Exception {
    List<AvisResponse> responses = avisService.getAllAvis();
    return ResponseEntity.ok(responses);
  }

  @Override
  public ResponseEntity<AvisResponse> _getAvisById(Long id) throws Exception {
    log.info("Received ID: " + id);
    AvisResponse avis = avisService.getAvisById(id);
    return ResponseEntity.ok(avis);
  }

  @Override
  public ResponseEntity<List<AvisResponse>> _getAvisByProductId(Long id) throws Exception {
    List<AvisResponse> avis = avisService.getAvisByProduitId(id);
    return ResponseEntity.ok(avis);
  }

  @Override
  public ResponseEntity<GetAvisByProductIdPaged200Response> _getAvisByProductIdPaged(Long id,
      Integer page, Integer size) throws Exception {
    GetAvisByProductIdPaged200Response avis = avisService.getAllAvisByProductId(id, page, size);
    return ResponseEntity.ok(avis);
  }

  @Override
  public ResponseEntity<List<AvisResponse>> _getAvisByUserId(Long id) throws Exception {
    List<AvisResponse> avis = avisService.getAvisByUtilisateurId(id);
    return ResponseEntity.ok(avis);
  }



  @Override
  public ResponseEntity<GetAvisPaged200Response> _getAvisPaged(String productName, String userEmail,
      String comment, Integer page, Integer size) throws Exception {
    GetAvisPaged200Response avis =
        avisService.getAllAvisPaged(productName, userEmail, comment, page, size);
    return ResponseEntity.ok(avis);
  }

  @Override
  public ResponseEntity<AvisResponse> _updateAvis(Long id, AvisRequest avisRequest)
      throws Exception {
    AvisResponse avis = avisService.updateAvis(id, avisRequest);
    return ResponseEntity.ok(avis);
  }
}
