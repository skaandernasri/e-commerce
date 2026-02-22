package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.CodePromoService;
import tn.temporise.domain.model.CodePromoRequest;
import tn.temporise.domain.model.CodePromoResponse;
import tn.temporise.domain.model.Response;
import tn.temporise.infrastructure.api.CodePromoApi;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class CodePromoController implements CodePromoApi {
  private final CodePromoService codePromoService;

  @Override
  @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GESTIONNAIRE')")
  public ResponseEntity<CodePromoResponse> _createCodePromo(CodePromoRequest codePromoRequest)
      throws Exception {
    log.info("Creating new promo code: {}", codePromoRequest.getCode());
    CodePromoResponse response = codePromoService.createCodePromo(codePromoRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Override
  @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GESTIONNAIRE')")
  public ResponseEntity<Response> _deleteAllCodePromos() throws Exception {
    log.info("Deleting all promo codes");
    codePromoService.deleteAllCodePromos();
    Response response = new Response();
    response.setCode("200");
    response.setMessage("Tous les codes promo ont été supprimés avec succès");
    return ResponseEntity.ok(response);
  }

  @Override
  @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GESTIONNAIRE')")
  public ResponseEntity<Response> _deleteCodePromo(Long id) throws Exception {
    log.info("Deleting promo code with ID: {}", id);
    codePromoService.deleteCodePromo(id);
    Response response = new Response();
    response.setCode("200");
    response.setMessage("Code promo supprimé avec succès");
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<List<CodePromoResponse>> _getAllCodePromo() throws Exception {
    return ResponseEntity.ok(codePromoService.getAllCodePromos());
  }

  @Override
  public ResponseEntity<CodePromoResponse> _getCodePromoById(Long id) throws Exception {
    log.info("Fetching promo code by ID: {}", id);
    CodePromoResponse response = codePromoService.getCodePromoById(id);
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<List<CodePromoResponse>> _getInActiveCodePromos() throws Exception {
    log.info("Fetching inactive promo codes");
    List<CodePromoResponse> responses = codePromoService.getInActiveCodePromos();
    return ResponseEntity.ok(responses);
  }

  @Override
  public ResponseEntity<CodePromoResponse> _getCodePromoByCode(String code) throws Exception {
    log.info("Fetching promo code by code: {}", code);
    CodePromoResponse response = codePromoService.getCodePromoByCode(code);
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<List<CodePromoResponse>> _getActiveCodePromos() throws Exception {
    log.info("Fetching active promo codes");
    List<CodePromoResponse> responses = codePromoService.getActiveCodePromos();
    return ResponseEntity.ok(responses);
  }

  @Override
  @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_GESTIONNAIRE')")
  public ResponseEntity<CodePromoResponse> _updateCodePromo(Long id,
      CodePromoRequest codePromoRequest) throws Exception {
    log.info("Updating promo code with ID: {}", id);
    CodePromoResponse response = codePromoService.updateCodePromo(id, codePromoRequest);
    return ResponseEntity.ok(response);
  }
}
