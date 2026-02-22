package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.PromotionService;
import tn.temporise.domain.model.PromotionRequest;
import tn.temporise.domain.model.PromotionResponse;
import tn.temporise.domain.model.Response;
import tn.temporise.infrastructure.api.PromotionsApi;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PromotionController implements PromotionsApi {
  private final PromotionService promotionService;

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<PromotionResponse> _createPromotion(PromotionRequest promotionRequest)
      throws Exception {
    PromotionResponse promotion = promotionService.createPromotion(promotionRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(promotion);
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<Response> _deleteAllPromotions() throws Exception {
    promotionService.deleteAllPromotions();
    Response response = new Response();
    response.setCode("200");
    response.setMessage("Toutes les promotions ont été supprimées avec succès");
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<Response> _deletePromotion(Long id) throws Exception {
    log.info("id---: " + id);
    promotionService.deletePromotion(id);
    Response responseBody = new Response();
    responseBody.setCode("200");
    responseBody.setMessage("Promotion supprimée !");
    return ResponseEntity.ok(responseBody);
  }

  @Override
  public ResponseEntity<List<PromotionResponse>> _getActivePromotions() throws Exception {
    List<PromotionResponse> responses = promotionService.getActivePromotions();
    return ResponseEntity.ok(responses);
  }

  @Override
  public ResponseEntity<List<PromotionResponse>> _getAllPromotions() throws Exception {
    List<PromotionResponse> responses = promotionService.getAllPromotions();
    return ResponseEntity.ok(responses);
  }

  @Override
  public ResponseEntity<List<PromotionResponse>> _getInActivePromotions() throws Exception {
    List<PromotionResponse> responses = promotionService.getInactivePromotions();
    return ResponseEntity.ok(responses);
  }

  @Override
  public ResponseEntity<PromotionResponse> _getPromotionById(Long id) throws Exception {
    log.info("Received ID: " + id);
    PromotionResponse promotion = promotionService.getPromotionById(id);
    return ResponseEntity.ok(promotion);
  }

  @Override
  public ResponseEntity<List<PromotionResponse>> _getPromotionByProductId(Long id)
      throws Exception {
    List<PromotionResponse> promotions = promotionService.getPromotionsByProduitId(id);
    return ResponseEntity.ok(promotions);
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<PromotionResponse> _updatePromotion(Long id,
      PromotionRequest promotionRequest) throws Exception {
    PromotionResponse promotion = promotionService.updatePromotion(id, promotionRequest);
    return ResponseEntity.ok(promotion);
  }
}
