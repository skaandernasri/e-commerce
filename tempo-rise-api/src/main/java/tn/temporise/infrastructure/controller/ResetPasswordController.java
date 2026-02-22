package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.ResetPasswordService;
import tn.temporise.domain.model.PasswordResetTokenRequest;
import tn.temporise.domain.model.Response;
import tn.temporise.infrastructure.api.ResetPasswordApi;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ResetPasswordController implements ResetPasswordApi {
  private final ResetPasswordService resetPasswordService;

  @Override
  public ResponseEntity<Response> _resetPassword(String token,
      PasswordResetTokenRequest passwordResetTokenRequest) throws Exception {
    resetPasswordService.resetPassword(token, passwordResetTokenRequest);
    Response response = new Response();
    response.setCode("200");
    response.setMessage("mot de passe reintialiser");
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Response> _validateResetToken(String token) throws Exception {
    resetPasswordService.validateResetToken(token);
    Response response = new Response();
    response.setCode("200");
    response.setMessage("token validé");
    return ResponseEntity.ok(response);
  }
}
