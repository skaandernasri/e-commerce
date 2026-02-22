package tn.temporise.infrastructure.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.EmailService;
import tn.temporise.domain.model.Response;
import tn.temporise.domain.model.SendEmailRequest;
import tn.temporise.infrastructure.api.SendEmailApi;


@RestController
@Slf4j
@RequiredArgsConstructor
public class EmailController implements SendEmailApi {
  private final EmailService emailService;

  @Override
  public ResponseEntity<Response> _activate(String token) throws Exception {
    emailService.activate(token);
    Response response = new Response();
    response.setCode("200");
    response.setMessage("email activé avec succès");
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Response> _sendPromoCodeEmail(SendEmailRequest sendEmailRequest)
      throws Exception {
    emailService.sendPromoCodeEmail(sendEmailRequest);
    Response response = new Response();
    response.setCode("200");
    response.setMessage("email activé avec succès");
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Response> _sendResetPasswordEmail(SendEmailRequest sendEmailRequest)
      throws Exception {
    emailService.sendResetPasswordEmail(sendEmailRequest);
    Response response = new Response();
    response.setCode("200");
    response.setMessage("email envoyé avec succès");
    return ResponseEntity.ok(response);
  }


  @Override
  public ResponseEntity<Response> _sendVerifEmail(SendEmailRequest sendEmailRequest)
      throws Exception {
    emailService.sendVerifEmail(sendEmailRequest);
    Response response = new Response();
    response.setCode("200");
    response.setMessage("email envoyé avec succès");
    return ResponseEntity.ok(response);
  }


}
