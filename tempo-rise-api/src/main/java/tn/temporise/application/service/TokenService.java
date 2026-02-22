package tn.temporise.application.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.exception.auth.UnauthorizedException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.application.mapper.AuthMapper;
import tn.temporise.domain.model.Authentification;
import tn.temporise.domain.model.CustomUserDetails;
import tn.temporise.domain.model.TokenResponse;
import tn.temporise.domain.port.AuthRepo;
import tn.temporise.infrastructure.persistence.entity.AuthentificationEntity;
import tn.temporise.infrastructure.security.utils.JwtUtil;



@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
  private final CustomUserDetailsService userDetailsService;
  private final JwtUtil jwtUtil;
  private final AuthRepo authRepo;
  private final AuthMapper authMapper;
  private final ExceptionFactory exceptionFactory;
  private final HttpServletResponse response;

  public TokenResponse refreshToken(HttpServletRequest request) {
    try {
      String oldAccessToken = jwtUtil.getJwtFromCookies(request);
      log.info("oldAccessToken: {}", oldAccessToken);
      if (oldAccessToken == null) {
        log.info("catched in oldAccessToken");
        throw exceptionFactory.notFound("notfound.token");
      }

      String email = jwtUtil.extractEmail(oldAccessToken);
      String providerId = jwtUtil.extractProviderId(oldAccessToken);
      if (email == null || providerId == null) {
        throw exceptionFactory.unauthorized("unauthorized.invalid_token");
      }

      CustomUserDetails userDetails =
          userDetailsService.loadUserByUsername(email + ";" + providerId);
      Authentification storedToken = authRepo.findByUserEmailAndProviderId(email, providerId);
      if (storedToken.token() == null) {
        log.info("catched in storedToken");
        throw exceptionFactory.notFound("notfound.token");
      }

      String refreshToken = storedToken.token();
      if (!jwtUtil.validateToken(refreshToken, userDetails)) {
        throw exceptionFactory.unauthorized("unauthorized.invalid_token");
      }

      String newAccessToken = jwtUtil.generateAccessToken(userDetails);
      String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);
      saveToken(email, newRefreshToken, providerId);

      TokenResponse tokenResponse = new TokenResponse();
      tokenResponse.setToken(newAccessToken);
      tokenResponse.setRefreshToken(newRefreshToken);
      jwtUtil.setJwtCookie(response, newAccessToken);
      return tokenResponse;

    } catch (BadRequestException | UnauthorizedException | NotFoundException e) {
      log.warn("Token refresh failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Token refresh failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", " Token refresh failed");
    }
  }

  public void saveToken(String email, String token, String providerId) {
    try {
      if (email == null || token == null || providerId == null) {
        throw exceptionFactory.badRequest("badrequest.missing_token");
      }

      Authentification authentification = authRepo.findByUserEmailAndProviderId(email, providerId);

      AuthentificationEntity authentification1 = authMapper.modelToEntity(authentification);
      authentification1.setRefreshToken(token);
      Authentification authentification2 =
          authRepo.save(authMapper.entityToModel(authentification1));
      log.info("Token saved: {}", authentification2);
    } catch (BadRequestException | NotFoundException e) {
      log.warn("Token save failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Token save failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public void removeToken(CustomUserDetails customUserDetails) {
    try {
      if (customUserDetails == null) {
        throw exceptionFactory.badRequest("badrequest.missing_token");
      }

      String providerId = customUserDetails.getProviderId();
      String email = customUserDetails.getUsername();

      Authentification authentification = authRepo.findByUserEmailAndProviderId(email, providerId);


      AuthentificationEntity authentification1 = authMapper.modelToEntity(authentification);
      authentification1.setRefreshToken(null);
      authRepo.save(authMapper.entityToModel(authentification1));

    } catch (BadRequestException e) {
      log.warn("Token removal failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Token removal failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }
}
