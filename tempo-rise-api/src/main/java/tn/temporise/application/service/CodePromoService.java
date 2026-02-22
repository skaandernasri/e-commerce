package tn.temporise.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.ConflictException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.application.mapper.CodePromoMapper;
import tn.temporise.domain.model.CodePromo;
import tn.temporise.domain.model.CodePromoRequest;
import tn.temporise.domain.model.CodePromoResponse;
import tn.temporise.domain.port.CodePromoRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodePromoService {
  private final CodePromoRepo codePromoRepo;
  private final CodePromoMapper codePromoMapper;
  private final ExceptionFactory exceptionFactory;

  public CodePromoResponse createCodePromo(CodePromoRequest codePromoRequest) {
    try {
      if (codePromoRequest == null || codePromoRequest.getCode() == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_request");
      }
      codePromoRequest.setCode(codePromoRequest.getCode().toUpperCase());
      CodePromo codePromo = codePromoMapper.dtoToModel(codePromoRequest);

      // Vérifier la date d'expiration
      if (codePromo.dateExpiration() != null && checkExpiration(codePromo.dateExpiration())) {
        throw exceptionFactory.badRequest("badrequest.promo_expired");
      }
      if (codePromoRepo.existsByCode(codePromo.code())) {
        throw exceptionFactory.conflict("conflict.code_promo_exist");
      }
      CodePromo savedCodePromo = codePromoRepo.save(codePromo);

      return codePromoMapper.modelToResponse(savedCodePromo);

    } catch (BadRequestException | NotFoundException | ConflictException e) {
      log.warn("CodePromo creation failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("CodePromo creation failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public CodePromoResponse getCodePromoById(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      CodePromo codePromo = codePromoRepo.findById(id);
      return codePromoMapper.modelToResponse(codePromo);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("CodePromo retrieval failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("CodePromo retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public CodePromoResponse getCodePromoByCode(String code) {
    try {
      if (code == null || code.isBlank()) {
        throw exceptionFactory.badRequest("badrequest.invalid_code");
      }

      // Implémentez cette méthode dans votre repository si nécessaire
      CodePromo codePromo = codePromoRepo.findByCode(code.toUpperCase());


      return codePromoMapper.modelToResponse(codePromo);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("CodePromo retrieval by code failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("CodePromo retrieval by code failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public List<CodePromoResponse> getAllCodePromos() {
    try {
      return codePromoRepo.findAll().stream().map(codePromoMapper::modelToResponse)
          .collect(Collectors.toList());

    } catch (NotFoundException e) {
      log.warn("No code promos found: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Code promo list retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public List<CodePromoResponse> getActiveCodePromos() {
    try {
      return codePromoRepo.findByExpirationDateGreaterThan(LocalDateTime.now()).stream()
          .map(codePromoMapper::modelToResponse).collect(Collectors.toList());

    } catch (NotFoundException e) {
      log.warn("No active code promos found: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Active code promo list retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public List<CodePromoResponse> getInActiveCodePromos() {
    try {
      return codePromoRepo.findByExpirationDateLessThanEqual(LocalDateTime.now()).stream()
          .map(codePromoMapper::modelToResponse).collect(Collectors.toList());

    } catch (NotFoundException e) {
      log.warn("No active code promos found: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Active code promo list retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }


  public CodePromoResponse updateCodePromo(Long id, CodePromoRequest codePromoRequest) {
    try {
      if (id == null || codePromoRequest == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }
      // Vérifier que le code promo existe
      codePromoRepo.findById(id);
      checkCode(id, codePromoRequest.getCode());
      codePromoRequest.setCode(codePromoRequest.getCode().toUpperCase());
      // Mapper les modifications
      CodePromo updatedCodePromo = codePromoMapper.dtoToModel(codePromoRequest);
      CodePromo codePromo = CodePromo.builder().id(id).code(updatedCodePromo.code())
          .dateExpiration(updatedCodePromo.dateExpiration()).reduction(updatedCodePromo.reduction())
          .build();


      // Sauvegarder les modifications
      CodePromo savedCodePromo = codePromoRepo.save(codePromo);

      return codePromoMapper.modelToResponse(savedCodePromo);

    } catch (BadRequestException | NotFoundException | ConflictException e) {
      log.warn("CodePromo update failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("CodePromo update failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public void deleteCodePromo(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      codePromoRepo.findById(id); // Vérifie l'existence
      codePromoRepo.deleteById(id);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("CodePromo deletion failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("CodePromo deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public void deleteAllCodePromos() {
    try {
      codePromoRepo.findAll(); // Vérifie qu'il y a des codes promos
      codePromoRepo.deleteAll();

    } catch (NotFoundException e) {
      log.warn("No code promos to delete: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Code promo bulk deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  private boolean checkExpiration(LocalDateTime date) {
    return date.isBefore(LocalDateTime.now());
  }

  private void checkCode(Long id, String code) throws ConflictException {
    try {
      CodePromo byCode = codePromoRepo.findByCode(code);
      if (byCode != null && !Objects.equals(byCode.id(), id)) {
        throw exceptionFactory.conflict("conflict.code_promo_exist");
      }
    } catch (NotFoundException ignore) {
    }
  }
}
