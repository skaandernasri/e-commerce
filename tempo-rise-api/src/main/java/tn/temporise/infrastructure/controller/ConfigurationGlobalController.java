package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.ConfigurationGlobalService;
import tn.temporise.domain.model.ConfigurationGlobalRequest;
import tn.temporise.domain.model.ConfigurationGlobalResponse;
import tn.temporise.infrastructure.api.ConfigurationGlobalApi;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ConfigurationGlobalController implements ConfigurationGlobalApi {
  private final ConfigurationGlobalService configurationGlobalService;

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<ConfigurationGlobalResponse> _createOrUpdateConfig(
      ConfigurationGlobalRequest configurationGlobalRequest) throws Exception {
    return ResponseEntity.ok(configurationGlobalService.saveConfig(configurationGlobalRequest));
  }

  @Override
  public ResponseEntity<ConfigurationGlobalResponse> _getConfig() throws Exception {
    return ResponseEntity.ok(configurationGlobalService.getConfig());
  }

}
