package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.SuiviClientService;
import tn.temporise.domain.model.Response;
import tn.temporise.domain.model.SuiviClientRequest;
import tn.temporise.infrastructure.api.SuiviClientApi;


@RestController
@Slf4j
@RequiredArgsConstructor
public class SuiviClientController implements SuiviClientApi {
  private final SuiviClientService suiviClientService;

  @Override
  public ResponseEntity<Response> _createSuiviClient(SuiviClientRequest suiviClientRequest)
      throws Exception {
    Response response = suiviClientService.saveSuiviClient(suiviClientRequest);
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Response> _mergeSuiviClient(SuiviClientRequest suiviClientRequest)
      throws Exception {
    return ResponseEntity.ok(suiviClientService.mergeAnonymousUserActions(suiviClientRequest));
  }
}
