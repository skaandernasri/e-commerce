package tn.temporise.application.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.auth.UnauthorizedException;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.PanierRepo;
import tn.temporise.domain.port.WhishlistRepo;
import tn.temporise.infrastructure.security.utils.JwtUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class SigninService {
  private final JwtUtil jwtUtil;
  private final ExceptionFactory exceptionFactory;
  private final HttpServletResponse response;
  private final CustomUserDetailsService userDetailsService;
  private final TokenService tokenService;
  private final HttpServletRequest httpServletRequest;
  private final PasswordEncoder passwordEncoder;
  private final PanierRepo panierRepo;
  private final WhishlistRepo whishlistRepo;

  public TokenResponse signinUser(SigninUserRequest request) {
    try {
      validateSigninRequest(request);

      CustomUserDetails userDetails =
          userDetailsService.loadUserByUsername(request.getEmail() + ";0" // "0" for local auth
          );
      log.info("userDetails username {}", userDetails.getUsername());
      validatePassword(request.getPassword(), userDetails.getPassword());
      UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
      log.info("authentication principal {}", userDetails.getUsername());
      SecurityContextHolder.getContext().setAuthentication(authToken);
      UtilisateurModel user = UtilisateurModel.builder().id(userDetails.id()).build();
      try {
        panierRepo.findByUtilisateurId(user.id());
      } catch (NotFoundException ignore) {
        panierRepo.save(Panier.builder().utilisateur(user).build());
      }
      try {
        whishlistRepo.findByUserId(user.id());

      } catch (NotFoundException ignore) {
        whishlistRepo.save(Whishlist.builder().utilisateur(user).build());
      }
      return generateTokenResponse(userDetails);
    } catch (NotFoundException | BadRequestException | UnauthorizedException e) {
      throw e;
    } catch (org.springframework.security.authentication.BadCredentialsException e) {
      throw exceptionFactory.badRequest("badrequest.invalid_credentials");
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public void validateSigninRequest(SigninUserRequest request) {
    if (request == null) {
      throw exceptionFactory.badRequest("badrequest.invalid_request");
    }
    if (request.getEmail() == null || request.getPassword() == null) {
      throw exceptionFactory.badRequest("badrequest.invalid_request");
    }
  }

  void validatePassword(String password, String encodedPassword) {
    if (!passwordEncoder.matches(password, encodedPassword)) {
      throw exceptionFactory.badRequest("badrequest.invalid_credentials");
    }
  }

  private TokenResponse generateTokenResponse(CustomUserDetails userDetails) {
    String token = jwtUtil.generateAccessToken(userDetails);
    String refreshToken = jwtUtil.generateRefreshToken(userDetails);

    jwtUtil.setJwtCookie(response, token);
    tokenService.saveToken(userDetails.getUsername(), refreshToken, userDetails.getProviderId());
    TokenResponse tokenResponse = new TokenResponse();
    tokenResponse.setToken(token);
    tokenResponse.setRefreshToken(refreshToken);
    return tokenResponse;
  }
}
