package tn.temporise.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.SuiviClientMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.ProductRepo;
import tn.temporise.domain.port.SuiviClientRepo;
import tn.temporise.domain.port.UserRepo;
import tn.temporise.domain.port.UtilisateurAnonymeRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuiviClientService {

  private final ExceptionFactory exceptionFactory;
  private final SuiviClientRepo suiviClientRepo;
  private final SuiviClientMapper suiviClientMapper;
  private final ProductRepo productRepo;
  private final UserRepo userRepo;
  private final UtilisateurAnonymeRepo utilisateurAnonymeRepo;

  public Response saveSuiviClient(SuiviClientRequest request) {
    try {
      if (request == null || request.getProduitId() == null || request.getTypeAction() == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }

      log.info("Saving suiviClientRequest: {}", request);
      Produit produit = productRepo.findById(request.getProduitId());
      SuiviClient suiviClientModel = suiviClientMapper.dtoToModel(request);
      double additionalScore = getScoreFromType(suiviClientModel.typeAction());

      if (request.getUtilisateurId() != null) {
        UtilisateurModel utilisateur = userRepo.findById(request.getUtilisateurId());

        Optional<SuiviClient> existingOpt = suiviClientRepo.findByUserAndProduitAndType(
            utilisateur.id(), produit.id(), suiviClientModel.typeAction());

        if (existingOpt.isPresent()) {
          SuiviClient existing = existingOpt.get();
          double newScore = existing.score() + additionalScore;
          SuiviClient updated =
              existing.toBuilder().score(newScore).date(LocalDateTime.now()).build();
          suiviClientRepo.save(updated);
          log.info("Updated existing SuiviClient with new score: {}", updated);
        } else {
          SuiviClient newSuivi = suiviClientMapper.dtoToModel(request).toBuilder()
              .utilisateur(utilisateur).utilisateurAnonyme(null).score(additionalScore)
              .date(LocalDateTime.now()).build();
          suiviClientRepo.save(newSuivi);
          log.info("Saved new SuiviClient: {}", newSuivi);
        }

      } else if (request.getUtilisateurAnonymeUuid() != null) {
        UtilisateurAnonyme utilisateurAnonyme =
            utilisateurAnonymeRepo.findBySessionToken(request.getUtilisateurAnonymeUuid());

        Optional<SuiviClient> existingOpt = suiviClientRepo.findByAnonymousAndProduitAndType(
            utilisateurAnonyme.id(), produit.id(), suiviClientModel.typeAction());

        if (existingOpt.isPresent()) {
          SuiviClient existing = existingOpt.get();
          double newScore = existing.score() + additionalScore;
          SuiviClient updated =
              existing.toBuilder().score(newScore).date(LocalDateTime.now()).build();
          suiviClientRepo.save(updated);
          log.info("Updated existing anonymous SuiviClient with new score: {}", updated);
        } else {
          SuiviClient newSuivi = suiviClientMapper.dtoToModel(request).toBuilder().utilisateur(null)
              .utilisateurAnonyme(utilisateurAnonyme).score(additionalScore)
              .date(LocalDateTime.now()).build();
          suiviClientRepo.save(newSuivi);
          log.info("Saved new anonymous SuiviClient: {}", newSuivi);
        }

      } else {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }

      Response response = new Response();
      response.setCode("201");
      response.setMessage("Suivi client saved or updated");
      return response;

    } catch (BadRequestException | NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public Response mergeAnonymousUserActions(SuiviClientRequest request) {
    try {
      if (request == null || request.getUtilisateurAnonymeUuid() == null
          || request.getUtilisateurId() == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }

      UtilisateurModel utilisateur = userRepo.findById(request.getUtilisateurId());
      UtilisateurAnonyme utilisateurAnonyme =
          utilisateurAnonymeRepo.findBySessionToken(request.getUtilisateurAnonymeUuid());

      List<SuiviClient> anonymousActions =
          suiviClientRepo.findByUtilisateurAnonymeId(utilisateurAnonyme.id());
      List<SuiviClient> userActions = suiviClientRepo.findByUtilisateurId(utilisateur.id());

      Set<String> existingKeys = userActions.stream()
          .map(sc -> sc.produit().id() + "-" + sc.typeAction().name()).collect(Collectors.toSet());

      for (SuiviClient sc : anonymousActions) {
        String key = sc.produit().id() + "-" + sc.typeAction().name();
        if (!existingKeys.contains(key)) {
          SuiviClient merged = SuiviClient.builder().utilisateur(utilisateur)
              .utilisateurAnonyme(null).produit(sc.produit()).typeAction(sc.typeAction())
              .score(sc.score()).date(LocalDateTime.now()).build();
          suiviClientRepo.save(merged);
        }
      }

      Response response = new Response();
      response.setCode("201");
      response.setMessage("Actions merged successfully");
      return response;

    } catch (NotFoundException | BadRequestException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  private double getScoreFromType(TypeAction typeAction) {
    return switch (typeAction) {
      case VIEW_PRODUCT -> 0.3;
      case ADD_TO_CART -> 0.7;
      case REMOVE_FROM_CART -> 0.5;
      case PURCHASE -> 1.0;
      case LEAVE_REVIEW -> 0.8;
      case SEARCH_CLICK -> 0.9;
      case PRODUCT_HOVER -> 0.4;
      default -> throw exceptionFactory.badRequest("badrequest.invalid_input");
    };
  }
}
