package tn.temporise.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.AdresseMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.AdresseRepo;
import tn.temporise.domain.port.UserRepo;
import tn.temporise.infrastructure.security.utils.CookiesUtil;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdresseService {
  private final AdresseRepo adresseRepo;
  private final ExceptionFactory exceptionFactory;
  private final AdresseMapper adresseMapper;
  private final UtilisateurAnonymeService utilisateurAnonymeService;
  private final CookiesUtil cookiesUtil;
  private final UserRepo userRepo;

  public AdresseResponse createAdresse(AdresseRequest adresseRequest) {
    try {
      if (adresseRequest == null || adresseRequest.getCodePostal() == null
          || adresseRequest.getVille() == null || adresseRequest.getType() == null
          || adresseRequest.getPays() == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }
      if (adresseRequest.getUtilisateurId() == null) {
        UtilisateurAnonymeResponse response = utilisateurAnonymeService
            .getOrCreateUtilisateurAnonyme(cookiesUtil.getUUIDCookieValue("anonyme_session_token"));

        log.info("Utilisateur anonyme created: {}", response);
        assert response != null;
        adresseRequest.setUtilisateurId(response.getId());
      } else {
        userRepo.findAllTypeOfUserById(adresseRequest.getUtilisateurId());
      }
      checkType(adresseRequest.getType());
      Adresse adresseToSave = adresseMapper.dtoToModel(adresseRequest);
      log.info("Adresse to save: {}", adresseToSave);
      Adresse savedAdresse = adresseRepo.save(adresseToSave);
      log.info("Adresse saved: {}", savedAdresse);
      AdresseResponse adresseResponse = adresseMapper.modelToDto(savedAdresse);
      log.info("Adresse response created: {}", adresseResponse);
      return adresseResponse;

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Adresse creation failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Adresse creation failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Adresse creation failed: " + e.getMessage());
    }
  }

  public AdresseResponse getAdresseById(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      return adresseMapper.modelToDto(adresseRepo.findById(id));

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Adresse retrieval failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Adresse retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Adresse retrieval failed: " + e.getMessage());
    }
  }

  public List<AdresseResponse> getAllAdresses() {
    try {


      return adresseRepo.findAll().stream().map(adresseMapper::modelToDto).toList();

    } catch (NotFoundException e) {
      log.warn("No addresses found: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Adresse list retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Adresse list retrieval failed: " + e.getMessage());
    }
  }

  public List<AdresseResponse> getAdressesByType(String type) {
    try {
      if (type == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }
      TypeAdresse typeAdresse = checkType(type);
      return adresseRepo.findByType(typeAdresse).stream().map(adresseMapper::modelToDto).toList();

    } catch (BadRequestException e) {
      log.warn("Adresse retrieval by type failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Adresse retrieval by type failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Adresse retrieval by type failed: " + e.getMessage());
    }
  }

  public List<AdresseResponse> getAdressesByUtilisateurId(Long utilisateurId) {
    try {
      if (utilisateurId == null) {
        UtilisateurAnonymeResponse response = utilisateurAnonymeService
            .getOrCreateUtilisateurAnonyme(cookiesUtil.getUUIDCookieValue("anonyme_session_token"));

        log.info("Utilisateur anonyme created: {}", response);
        assert response != null;
        utilisateurId = response.getId();
      }

      return adresseRepo.findByUtilisateurId(utilisateurId).stream().map(adresseMapper::modelToDto)
          .toList();

    } catch (Exception e) {
      log.error("Adresse retrieval by user failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Adresse retrieval by user failed: " + e.getMessage());
    }
  }

  public AdresseResponse updateAdresse(Long id, AdresseRequest adresse) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }
      Adresse adressDB = adresseRepo.findById(id);
      adressDB = adressDB.toBuilder().ligne1(adresse.getLigne1()).ligne2(adresse.getLigne2())
          .ville(adresse.getVille()).codePostal(adresse.getCodePostal()).pays(adresse.getPays())
          .build();
      Adresse savedAdresse = adresseRepo.save(adressDB);
      log.info("saved adresse: {}", savedAdresse);
      AdresseResponse adresseResponse = adresseMapper.modelToDto(savedAdresse);
      log.info("Adresse response created: {}", adresseResponse);
      return adresseResponse;

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Adresse update failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Adresse update failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Adresse update failed: " + e.getMessage());
    }
  }

  @Transactional
  public void deleteAdresse(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      adresseRepo.findById(id);
      adresseRepo.deleteById(id);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Adresse deletion failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Adresse deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Adresse deletion failed: " + e.getMessage());
    }
  }

  public void deleteAllAdresses() {
    try {
      List<Adresse> adresses = adresseRepo.findAll();

      adresseRepo.deleteAll();

    } catch (NotFoundException e) {
      log.warn("No addresses to delete: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Adresse bulk deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Adresse bulk deletion failed: " + e.getMessage());
    }
  }

  private TypeAdresse checkType(String type) {
    return switch (type.toUpperCase()) {
      case "LIVRAISON" -> TypeAdresse.LIVRAISON;
      case "FACTURATION" -> TypeAdresse.FACTURATION;
      default -> throw exceptionFactory.badRequest("badrequest.invalid_input");
    };
  }

}
