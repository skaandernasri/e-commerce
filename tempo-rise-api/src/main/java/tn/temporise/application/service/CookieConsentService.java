package tn.temporise.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.GetOrCreateCookieConsent200Response;
import tn.temporise.domain.model.GetOrCreateCookieConsentRequest;
import tn.temporise.infrastructure.security.utils.CookiesUtil;


@Slf4j
@Service
@RequiredArgsConstructor
public class CookieConsentService {
  private final ExceptionFactory exceptionFactory;
  private final CookiesUtil cookiesUtil;

  private ResponseCookie createCookieConsent(String cookieValue) {

    return cookiesUtil.createCookie(cookieValue, "cookie_consent");
  }

  public ResponseEntity<GetOrCreateCookieConsent200Response> getOrCreateCookieConsent(
      GetOrCreateCookieConsentRequest request, String cookieValue) {
    try {
      GetOrCreateCookieConsent200Response response = new GetOrCreateCookieConsent200Response();
      // Case 1: User already has consent cookie
      if (cookieValue != null) {
        response.setCookieConsentExist(true);
        return ResponseEntity.ok(response); // No need to resend cookie
      }

      // Case 2: User does NOT have cookie but frontend gives consent
      if (request != null && request.getValue() != null) {
        ResponseCookie cookie = createCookieConsent(request.getValue().getValue());
        response.setCookieConsentExist(true);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(response);
      }

      // Case 3: User has NO cookie and frontend did NOT give consent
      response.setCookieConsentExist(false);
      return ResponseEntity.ok(response); // No cookie sent
    } catch (BadRequestException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

}
