package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.CategorieService;
import tn.temporise.domain.model.CategorieRequest;
import tn.temporise.domain.model.CategorieResponse;
import tn.temporise.domain.model.Response;
import tn.temporise.infrastructure.api.CategoriesApi;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CategorieController implements CategoriesApi {
  private final CategorieService categorieService;

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<CategorieResponse> _createCategory(CategorieRequest categorieRequest)
      throws Exception {
    CategorieResponse categorie = categorieService.createCategorie(categorieRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(categorie);
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<Response> _deleteAllCategories() throws Exception {
    categorieService.deleteAllCategories();
    Response response = new Response();
    response.setCode("200");
    response.setMessage("Toutes les catégories ont été supprimés avec succès");
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<Response> _deleteCategory(Long id) throws Exception {
    log.info("id---: " + id);
    categorieService.deleteCategorie(id);
    Response responseBody = new Response();
    responseBody.setCode("200");
    responseBody.setMessage("Catégorie supprimé !");
    return ResponseEntity.ok(responseBody);
  }

  @Override
  public ResponseEntity<List<CategorieResponse>> _getAllCategories() throws Exception {
    List<CategorieResponse> responses = categorieService.getAllCategories();
    return ResponseEntity.ok(responses);
  }

  // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<CategorieResponse> _getCategoryById(Long id) throws Exception {
    log.info("Received ID: " + id);
    CategorieResponse categorie = categorieService.getCategorieById(id);
    return ResponseEntity.ok(categorie);
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<CategorieResponse> _updateCategory(Long id,
      CategorieRequest categorieRequest) throws Exception {
    CategorieResponse categorie = categorieService.updateCategorie(id, categorieRequest);
    return ResponseEntity.ok(categorie);
  }
}
