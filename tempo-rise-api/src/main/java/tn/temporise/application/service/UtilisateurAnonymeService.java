package tn.temporise.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.UtilisateurAnonymeMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.UserType;
import tn.temporise.domain.model.UtilisateurAnonyme;
import tn.temporise.domain.model.UtilisateurAnonymeResponse;
import tn.temporise.domain.port.UtilisateurAnonymeRepo;
import tn.temporise.infrastructure.security.utils.CookiesUtil;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UtilisateurAnonymeService {
  private final UtilisateurAnonymeRepo utilisateurAnonymeRepo;
  private final ExceptionFactory exceptionFactory;
  private final UtilisateurAnonymeMapper utilisateurAnonymeMapper;
  private final CookiesUtil cookiesUtil;

  public UtilisateurAnonymeResponse getOrCreateUtilisateurAnonyme(UUID sessionToken) {
    try {
      UtilisateurAnonymeResponse responseDto;

      if (sessionToken == null) {
        responseDto = createUtilisateurAnonyme();
      } else {
        try {
          responseDto = getUtilisateurAnonyme(sessionToken);
        } catch (NotFoundException e) {
          responseDto = createUtilisateurAnonyme();
        }
      }
      ResponseCookie cookie = cookiesUtil.createCookie(responseDto.getSessionToken().toString(),
          "anonyme_session_token");
      log.info("Cookie created: {}", cookie);
      return responseDto;

    } catch (BadRequestException e) {
      log.warn("UtilisateurAnonyme creation failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("UtilisateurAnonyme creation failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  private UtilisateurAnonymeResponse createUtilisateurAnonyme() {
    UUID uuid = UUID.randomUUID();
    String email = uuid + "@temposphere.tn";
    UtilisateurAnonyme utilisateurAnonyme = UtilisateurAnonyme.builder().sessionToken(uuid)
        .email(email).userType(UserType.ANONYMOUS).build();
    return utilisateurAnonymeMapper.modelToDto(utilisateurAnonymeRepo.save(utilisateurAnonyme));
  }

  public UtilisateurAnonymeResponse getUtilisateurAnonyme(UUID sessionToken)
      throws NotFoundException {
    UtilisateurAnonyme utilisateurAnonyme = utilisateurAnonymeRepo.findBySessionToken(sessionToken);
    return utilisateurAnonymeMapper.modelToDto(utilisateurAnonyme);
  }


}
