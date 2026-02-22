package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.WhishlistService;
import tn.temporise.domain.model.Response;
import tn.temporise.domain.model.WhishlistDto;
import tn.temporise.infrastructure.api.WhishlistApi;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WhishlistController implements WhishlistApi {
  private final WhishlistService whishlistService;

  @Override
  public ResponseEntity<Response> _addProductToWhishlist(Long productId, Long userId)
      throws Exception {
    Response response = whishlistService.addItemToWhishlist(userId, productId);
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<WhishlistDto> _getWhishlistByUserId(Long userId) throws Exception {
    WhishlistDto whishlistDto = whishlistService.getWhishlist(userId);
    return ResponseEntity.ok(whishlistDto);
  }

  @Override
  public ResponseEntity<Response> _removeAllProductsFromWhishlist(Long userId) throws Exception {
    Response response = whishlistService.removeAllItemsFromWhishlist(userId);
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Response> _removeProductFromWhishlist(Long productId, Long userId)
      throws Exception {
    Response response = whishlistService.removeItemFromWhishlist(userId, productId);
    return ResponseEntity.ok(response);
  }
}
