package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.UtilisateurAnonymeService;
import tn.temporise.domain.model.UtilisateurAnonymeResponse;
import tn.temporise.infrastructure.api.UtilisateurAnonymeApi;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UtilisateurAnonymeController implements UtilisateurAnonymeApi {
  private final UtilisateurAnonymeService utilisateurAnonymeService;

  @Override
  public ResponseEntity<UtilisateurAnonymeResponse> _getOrCreateAnonyme(
      @CookieValue(value = "anonyme_session_token", required = false) UUID sessionToken)
      throws Exception {
    UtilisateurAnonymeResponse response =
        utilisateurAnonymeService.getOrCreateUtilisateurAnonyme(sessionToken);
    return ResponseEntity.ok().body(response);
  }
}
