package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.PanierService;
import tn.temporise.domain.model.CartItemRequest;
import tn.temporise.domain.model.PanierResponse;
import tn.temporise.domain.model.Response;
import tn.temporise.infrastructure.api.PaniersApi;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PanierController implements PaniersApi {
  private final PanierService panierService;

  @Override
  public ResponseEntity<PanierResponse> _addProductToPanier(Long userId, Long productId,
      CartItemRequest item) throws Exception {
    PanierResponse panierResponse = panierService.addItem(userId, productId, item);
    return ResponseEntity.status(HttpStatus.CREATED).body(panierResponse);
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<Response> _deleteAllPaniers() throws Exception {
    panierService.deleteAllPaniers();
    Response response = new Response();
    response.setCode("200");
    response.setMessage("Tout les paniers ont été supprimés avec succès");
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<Response> _deletePanier(Long id) throws Exception {
    log.info("Deleting Panier with ID: {}", id);
    panierService.deletePanier(id);
    Response response = new Response();
    response.setCode("200");
    response.setMessage("Panier supprimé avec succès !");
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<List<PanierResponse>> _getAllPaniers() throws Exception {
    log.info("Fetching all Paniers");
    List<PanierResponse> panierResponses = panierService.getAllPaniers();
    return ResponseEntity.ok(panierResponses);
  }

  @Override
  public ResponseEntity<PanierResponse> _getPanierById(Long id) throws Exception {
    log.info("Fetching Panier with ID: {}", id);
    PanierResponse panierResponse = panierService.getPanierById(id);
    log.info("PanierResponse: {}", panierResponse);
    return ResponseEntity.ok(panierResponse);
  }

  @Override
  public ResponseEntity<PanierResponse> _getPanierByUserId(Long id) throws Exception {
    log.info("Fetching Panier with User ID: {}", id);
    PanierResponse panierResponses = panierService.getOrCreateCart(id);
    return ResponseEntity.ok(panierResponses);
  }

  @Override
  public ResponseEntity<List<Response>> _isProductAvailable() throws Exception {
    return null;
  }

  @Override
  public ResponseEntity<PanierResponse> _mergePanier(Long userId,
      List<CartItemRequest> cartItemRequest) throws Exception {
    log.info("Fetching Panier with User ID: {}", userId);
    PanierResponse panierResponses = panierService.mergeCart(userId, cartItemRequest);
    return ResponseEntity.ok(panierResponses); // Return the PanierResponseE;
  }

  @Override
  public ResponseEntity<Response> _removeAllItemFromCart(Long userId, Long panierId)
      throws Exception {
    panierService.removeAllItemsFromPanier(userId, panierId);
    Response response = new Response();
    response.setCode("200");
    response.setMessage("Tout les produits sont supprimés du panier avec succès !");
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Response> _removeItemFromCart(Long userId, Long productId)
      throws Exception {
    panierService.removeItem(userId, productId);
    Response response = new Response();
    response.setCode("200");
    response.setMessage("produit supprimé du panier avec succès !");
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<PanierResponse> _updateCartItem(Long userId, Long productId,
      CartItemRequest cartItemRequest) throws Exception {
    PanierResponse panierResponse =
        panierService.updateItemQuantity(userId, productId, cartItemRequest);
    log.info("panierResponse in updateCartItem: {}", panierResponse);
    return ResponseEntity.ok(panierResponse);
  }
}
