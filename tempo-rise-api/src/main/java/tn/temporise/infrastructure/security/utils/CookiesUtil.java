package tn.temporise.infrastructure.security.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CookiesUtil {
  private final HttpServletRequest httpServletRequest;
  private final HttpServletResponse httpServletResponse;

  public String getCookieValue(String cookieName) {
    Cookie[] cookies = httpServletRequest.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(cookieName)) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  public UUID getUUIDCookieValue(String cookieName) {
    String value = getCookieValue(cookieName);
    if (value == null) {
      return null;
    }
    return UUID.fromString(value);
  }

  public ResponseCookie createCookie(String cookieValue, String cookieName) {
    ResponseCookie cookie = ResponseCookie.from(cookieName, cookieValue).httpOnly(true).secure(true) // only
                                                                                                     // if
                                                                                                     // using
                                                                                                     // //
                                                                                                     // HTTPS
        .sameSite("Lax").path("/").maxAge(365 * 24 * 60 * 60) // 1 year
        .build();
    httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    return cookie;
  }
}
