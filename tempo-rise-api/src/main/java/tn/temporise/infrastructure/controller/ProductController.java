package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.ProductService;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.api.ProduitsApi;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ProductController implements ProduitsApi {
  private final ProductService productService;

  @Override
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<ProductResponse> _createProduct(ProductRequest productRequest)
      throws Exception {
    ProductResponse produit = productService.createProduct(productRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(produit);
  }

  @Override
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Response> _deleteAllProducts() throws Exception {
    productService.deleteAllProducts();
    Response response = new Response();
    response.setCode("200");
    response.setMessage("Tous les produits ont été supprimés avec succès");
    return ResponseEntity.ok(response);
  }

  @Override
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<Response> _deleteProduct(Long id) throws Exception {
    log.info("id---: " + id);
    productService.deleteProduct(id);
    Response responseBody = new Response();
    responseBody.setCode("200");
    responseBody.setMessage("Produit supprimé !");
    return ResponseEntity.ok(responseBody);
  }

  @Override
  public ResponseEntity<List<ProductResponse>> _getAllProducts() throws Exception {
    List<ProductResponse> responses = productService.getAllProducts();
    return ResponseEntity.ok(responses);
  }

  @Override
  public ResponseEntity<List<ProductResponse>> _getFilteredProducts(String productName,
      List<String> categoryNames, Boolean actif) throws Exception {
    List<ProductResponse> responses =
        productService.getFilteredProducts(productName, categoryNames, actif);
    return ResponseEntity.ok(responses);
  }

  @Override
  public ResponseEntity<GetFilteredProductsPage200Response> _getFilteredProductsPage(Integer page,
      Integer size, String productName, List<String> categoryNames, Double minPrice,
      Double maxPrice, Double minPromotion, Double maxPromotion, Boolean actif,
      Boolean orderByRatingDesc) throws Exception {
    GetFilteredProductsPage200Response response =
        productService.getAllProductPagedAndFiltered(page, size, productName, categoryNames,
            minPrice, maxPrice, minPromotion, maxPromotion, actif, orderByRatingDesc);
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<ProductResponse> _getProductById(Long id) throws Exception {
    log.info("Received ID: " + id);
    ProductResponse produit = productService.getProductById(id);
    return ResponseEntity.ok(produit);
  }

  @Override
  public ResponseEntity<List<ProductResponse>> _getProductsByIds(List<Long> id) throws Exception {
    return ResponseEntity.ok(productService.getProductsByIds(id));
  }

  // @Override
  // public ResponseEntity<GetProductsPage200Response> _getProductsPage(Integer number, Integer
  // size)
  // throws Exception {
  // return ResponseEntity.ok(productService.getAllPaginatedProducts(number, size));
  // }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<GetStockDetails200Response> _getStockDetails(Long stockMoreThan,
      Long stockLessThan, Long stockEquals) throws Exception {
    GetStockDetails200Response response =
        productService.getStockDetails(stockMoreThan, stockLessThan, stockEquals);
    return ResponseEntity.ok(response);
  }


  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<ProductResponse> _updateProduit(Long id, ProductRequest productRequest)
      throws Exception {
    ProductResponse produit = productService.updateProduct(id, productRequest);
    return ResponseEntity.ok(produit);
  }
}
