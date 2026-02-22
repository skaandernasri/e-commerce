package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.GoogleAnalyticsService;
import tn.temporise.domain.model.Response;
import tn.temporise.infrastructure.api.AnalyticsApi;

@RequiredArgsConstructor
@Slf4j
@RestController
public class AnalyticsController implements AnalyticsApi {
  private final GoogleAnalyticsService googleAnalyticsService;

  @Override
  public ResponseEntity<Response> _analytics() throws Exception {
    googleAnalyticsService.fetchAnalytics();
    Response response = new Response();
    response.setMessage("success");
    return ResponseEntity.ok(response);
  }
}
