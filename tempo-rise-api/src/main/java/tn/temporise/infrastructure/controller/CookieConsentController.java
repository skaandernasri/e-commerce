package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.CookieConsentService;
import tn.temporise.domain.model.GetOrCreateCookieConsent200Response;
import tn.temporise.domain.model.GetOrCreateCookieConsentRequest;
import tn.temporise.infrastructure.api.CookieConsentApi;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CookieConsentController implements CookieConsentApi {
  private final CookieConsentService cookieConsentService;

  @Override
  public ResponseEntity<GetOrCreateCookieConsent200Response> _getOrCreateCookieConsent(
      @CookieValue(value = "cookie_consent", required = false) String cookieConsent,
      GetOrCreateCookieConsentRequest getOrCreateCookieConsentRequest) throws Exception {
    return cookieConsentService.getOrCreateCookieConsent(getOrCreateCookieConsentRequest,
        cookieConsent);
  }
}
