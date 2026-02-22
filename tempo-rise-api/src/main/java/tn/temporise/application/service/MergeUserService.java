package tn.temporise.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.Response;
import tn.temporise.domain.model.UtilisateurModel;
import tn.temporise.domain.port.CommandeRepo;
import tn.temporise.domain.port.UserRepo;
import tn.temporise.domain.port.UtilisateurAnonymeRepo;

@Service
@Slf4j
@RequiredArgsConstructor
public class MergeUserService {
  private final CommandeRepo commandeRepo;
  private final ExceptionFactory exceptionFactory;
  private final UserRepo userRepo;
  private final UtilisateurAnonymeRepo utilisateurAnonymeRepo;

  public Response mergeUserCommande(Long idUser, Long idUserAnonyme) {
    try {
      if (idUser == null || idUserAnonyme == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }
      UtilisateurModel user = userRepo.findById(idUser);
      utilisateurAnonymeRepo.findById(idUserAnonyme);
      commandeRepo.findByUserId(idUserAnonyme).forEach(commande -> {
        commandeRepo.save(commande.toBuilder().user(user).build());
      });
      Response response = new Response();
      response.setCode("200");
      response.setMessage("Commandes mergées avec succé");
      return response;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }
}
