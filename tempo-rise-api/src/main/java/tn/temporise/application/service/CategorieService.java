package tn.temporise.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.application.mapper.CategorieMapper;
import tn.temporise.domain.model.Categorie;
import tn.temporise.domain.model.CategorieRequest;
import tn.temporise.domain.model.CategorieResponse;
import tn.temporise.domain.port.CategorieRepo;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategorieService {
  private final CategorieRepo categorieRepo;
  private final CategorieMapper categorieMapper;
  private final ExceptionFactory exceptionFactory;

  public CategorieResponse createCategorie(CategorieRequest categorieRequest) {
    try {
      if (categorieRequest == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }

      Categorie categorie = categorieMapper.dtoToModel(categorieRequest);
      Categorie savedCategorie = categorieRepo.save(categorie);

      if (savedCategorie == null) {
        throw exceptionFactory.internalServerError("internal.server_error",
            "Category creation failed");
      }

      return categorieMapper.modelToResponse(savedCategorie);

    } catch (BadRequestException e) {
      log.warn("Category creation failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Category creation failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Category creation failed" + e.getMessage());
    }
  }

  public CategorieResponse getCategorieById(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      Categorie categorie = categorieRepo.findById(id);
      if (categorie == null) {
        throw exceptionFactory.notFound("notfound.category");
      }
      return categorieMapper.modelToResponse(categorie);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Category retrieval failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Category retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Category retrieval failed" + e.getMessage());
    }
  }

  public List<CategorieResponse> getAllCategories() {
    try {
      List<CategorieResponse> categorieList =
          categorieRepo.findAll().stream().map(categorieMapper::modelToResponse).toList();

      if (categorieList.isEmpty()) {
        throw exceptionFactory.notFound("notfound.no_categories");
      }
      return categorieList;

    } catch (NotFoundException e) {
      log.warn("No categories found", e);
      throw e;
    } catch (Exception e) {
      log.error("Category list retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Category list retrieval failed" + e.getMessage());
    }
  }

  public CategorieResponse updateCategorie(Long id, CategorieRequest categorieRequest) {
    try {
      if (id == null || categorieRequest == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }

      Categorie existingCategorie = categorieRepo.findById(id);
      if (existingCategorie == null) {
        throw exceptionFactory.notFound("notfound.category");
      }

      categorieRequest.setId(id);
      Categorie updatedCategorie = categorieMapper.dtoToModel(categorieRequest);
      updatedCategorie = categorieRepo.update(updatedCategorie);

      if (updatedCategorie == null) {
        throw exceptionFactory.internalServerError("internal.server_error",
            "Category update failed");
      }

      return categorieMapper.modelToResponse(updatedCategorie);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Category update failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Category update failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Category update failed" + e.getMessage());
    }
  }

  public void deleteCategorie(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      Categorie categorie = categorieRepo.findById(id);
      if (categorie == null) {
        throw exceptionFactory.notFound("notfound.category");
      }

      categorieRepo.deleteById(id);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Category deletion failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Category deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Category deletion failed" + e.getMessage());
    }
  }

  public void deleteAllCategories() {
    try {
      List<Categorie> categorieList = categorieRepo.findAll();
      if (categorieList.isEmpty()) {
        throw exceptionFactory.notFound("notfound.no_categories");
      }
      categorieRepo.deleteAll();

    } catch (NotFoundException e) {
      log.warn("No categories to delete", e);
      throw e;
    } catch (Exception e) {
      log.error("Category bulk deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Category bulk deletion failed" + e.getMessage());
    }
  }
}
