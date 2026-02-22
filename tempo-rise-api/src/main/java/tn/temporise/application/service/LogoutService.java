package tn.temporise.application.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.auth.UnauthorizedException;
import tn.temporise.application.exception.client.ConflictException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.CustomUserDetails;
import tn.temporise.infrastructure.security.utils.JwtRequestFilter;
import tn.temporise.infrastructure.security.utils.JwtUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {
  private final TokenService tokenService;
  private final JwtUtil jwtUtil;
  private final ExceptionFactory exceptionFactory;
  private final JwtRequestFilter jwtRequestFilter;
  private static final String JWT_COOKIE_NAME = "jwt";

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) {
    try {
      log.info("Logging out user");
      String token = jwtUtil.getJwtFromCookies(request);

      if (token != null) {
        if (!jwtUtil.isTokenStructureValid(token)) {
          throw exceptionFactory.unauthorized("unauthorized.token_malformed");
        }
        if (jwtUtil.isTokenExpired(token)) {
          throw exceptionFactory.unauthorized("unauthorized.token_expired");
        }
        // Only remove the specific token
        tokenService.removeToken(CustomUserDetails.builder().email(jwtUtil.extractEmail(token))
            .providerId(jwtUtil.extractProviderId(token)).build());
        jwtUtil.removeJwtCookie(response);
      }

      // Don't clear entire security context
    } catch (NotFoundException | ConflictException | UnauthorizedException e) {
      jwtUtil.removeJwtCookie(response);
      throw e;
    } catch (Exception e) {
      log.error("Logout failed", e);
      jwtUtil.removeJwtCookie(response);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }
}
