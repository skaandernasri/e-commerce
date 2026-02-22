package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.VariantService;
import tn.temporise.domain.model.Response;
import tn.temporise.domain.model.VariantResponse;
import tn.temporise.infrastructure.api.VariantApi;


@RestController
@RequiredArgsConstructor
@Slf4j
public class VariantController implements VariantApi {
  private final VariantService variantService;

  @Override
  public ResponseEntity<Response> _deleteVariant(Long variantId) throws Exception {
    return ResponseEntity.ok(variantService.deleteVariant(variantId));
  }

  @Override
  public ResponseEntity<VariantResponse> _getVariant(Long variantId) throws Exception {
    return ResponseEntity.ok(variantService.getVariant(variantId));
  }
  // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  // @Override
  // public ResponseEntity<GetStockDetails200Response> _getStockDetails(Long stockMoreThan,
  // Long stockLessThan, Long stockEquals) throws Exception {
  // GetStockDetails200Response response =
  // variantService.getStockDetails(stockMoreThan, stockLessThan, stockEquals);
  // return ResponseEntity.ok(response);
  // }


}
