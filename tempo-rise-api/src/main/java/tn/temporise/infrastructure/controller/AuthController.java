package tn.temporise.infrastructure.controller;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.*;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.api.AuthentificationApi;
import tn.temporise.infrastructure.security.utils.JwtUtil;

import java.util.Arrays;


@RequiredArgsConstructor
@Slf4j
@RestController
public class AuthController implements AuthentificationApi {

  private final AuthenticationManager authenticationManager;
  private final RegistrationService registrationService;
  private final HttpServletRequest request;
  private final TokenService tokenService;
  private final CustomOAuth2UserService customOAuth2UserService;
  private final JwtUtil jwtUtil;
  private final SigninService signinService;
  private final RecaptchaService recaptchaService;
  @Value("${prod}")
  private boolean prod;

  @Override
  public ResponseEntity<Response> _logoutUser() throws Exception {
    log.info("User logged out successfully" + request.getHeader("set-cookie"));
    Response responseBody = new Response();
    responseBody.setCode("200");
    responseBody.setMessage("Déconnecter avec succés");
    return ResponseEntity.ok().body(responseBody);

  }

  @Override
  public ResponseEntity<Response> _refreshToken(Object object) throws Exception {
    TokenResponse tokenResponse = tokenService.refreshToken(request);
    String token = tokenResponse.getToken();
    Response response = new Response();
    response.setCode("200");
    response.setMessage("token rafraichi avec succès");
    response.getDetails()
        .addAll(Arrays.asList(jwtUtil.extractEmail(token), jwtUtil.extractRoles(token).toString(),
            jwtUtil.extractUsername(token), jwtUtil.extractId(token),
            jwtUtil.extractExpiration(token).toString()));
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Response> _signinFacebook(SigninFacebookRequest signinFacebookRequest)
      throws Exception {
    // Implementation for Facebook sign-in
    return null;
  }

  @Override
  public ResponseEntity<Response> _signinGoogle(SigninGoogleRequest signinGoogleRequest)
      throws Exception {
    // Case 1: Initial OAuth2 flow trigger
    TokenResponse tokenResponse =
        customOAuth2UserService.signinGoogle(signinGoogleRequest.getIdToken());
    String token = tokenResponse.getToken();
    Response response = new Response();
    response.setCode("200");
    response.setMessage("Google authentication successful");
    response.getDetails()
        .addAll(Arrays.asList(jwtUtil.extractEmail(token), jwtUtil.extractRoles(token).toString(),
            jwtUtil.extractUsername(token), jwtUtil.extractId(token),
            jwtUtil.extractExpiration(token).toString()));
    // response.getDetails().add()

    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Response> _signinUser(SigninUserRequest signinUserRequest)
      throws Exception {
    if (prod)
      recaptchaService.verifyToken(signinUserRequest.getReCaptchaToken());
    TokenResponse tokenResponse = signinService.signinUser(signinUserRequest);
    String token = tokenResponse.getToken();
    Response response = new Response();
    response.setCode("200");
    response.getDetails()
        .addAll(Arrays.asList(jwtUtil.extractEmail(token), jwtUtil.extractRoles(token).toString(),
            jwtUtil.extractUsername(token), jwtUtil.extractId(token),
            jwtUtil.extractExpiration(token).toString()));
    log.info("User signed in successfully: {}", response.getDetails());
    response.setMessage("Connecté avec succés");
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Response> _signupUser(SignupUserRequest signupUserRequest)
      throws Exception {
    if (prod)
      recaptchaService.verifyToken(signupUserRequest.getReCaptchaToken());
    registrationService.register(signupUserRequest);
    log.info("User registered successfully");
    Response response = new Response();
    response.setCode("201");
    response.setMessage("utilisateur crée avec succés");
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

}
