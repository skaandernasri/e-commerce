package tn.temporise.infrastructure.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import tn.temporise.application.exception.auth.UnauthorizedException;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.ConflictException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.exception.client.PasswordException;
import tn.temporise.application.exception.domain.ProviderException;
import tn.temporise.application.exception.domain.RegistrationException;
import tn.temporise.application.exception.external.CaptchaException;
import tn.temporise.application.exception.external.PaymentException;
import tn.temporise.application.exception.server.InternalServerErrorException;
import tn.temporise.application.exception.server.LogoutException;
import tn.temporise.domain.model.Response;

@ControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

  private <T extends RuntimeException> ResponseEntity<Response> buildErrorResponse(T ex,
      HttpStatus status, String code) {
    Response response = new Response();
    response.setMessage(ex.getMessage());
    response.setCode(code);
    return ResponseEntity.status(status).body(response);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<Response> handleMaxSizeException(MaxUploadSizeExceededException exc) {
    return buildErrorResponse(exc, HttpStatus.PAYLOAD_TOO_LARGE, "99999");
  }

  @ExceptionHandler(RegistrationException.class)
  public ResponseEntity<Response> handelRegistrationException(RegistrationException ex) {
    return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, ex.getCode());
  }


  @ExceptionHandler(PasswordException.class)
  public ResponseEntity<Response> handelPasswordException(PasswordException ex) {
    return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, ex.getCode());
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Response> handelNotFoundException(NotFoundException ex) {
    return buildErrorResponse(ex, HttpStatus.NOT_FOUND, ex.getCode());
  }


  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<Response> handleUnauthorizedException(UnauthorizedException ex) {
    return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, ex.getCode());
  }

  @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
  public ResponseEntity<Response> handleAccessDeniedException(AccessDeniedException ex) {
    Response response = new Response();
    response.setCode("4003");
    response.setMessage(ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  @ExceptionHandler(PaymentException.class)
  public ResponseEntity<Response> paymentException(PaymentException ex) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    if (ex.getCode().equals("4063"))
      status = HttpStatus.NOT_FOUND;
    if (ex.getCode().equals("4016"))
      status = HttpStatus.UNAUTHORIZED;
    if (ex.getCode().equals("422"))
      status = HttpStatus.UNPROCESSABLE_ENTITY;
    return buildErrorResponse(ex, status, ex.getCode());
  }

  @ExceptionHandler(CaptchaException.class)
  public ResponseEntity<Response> handleCaptchaException(CaptchaException ex) {
    return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, ex.getCode());
  }

  @ExceptionHandler(InternalServerErrorException.class)
  public ResponseEntity<Response> handleInternalServerErrorException(
      InternalServerErrorException ex) {
    return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, ex.getCode());
  }


  @ExceptionHandler(LogoutException.class)
  public ResponseEntity<Response> handleLogoutException(LogoutException ex) {
    return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, ex.getCode());
  }

  @ExceptionHandler(ProviderException.class)
  public ResponseEntity<Response> handleProviderException(ProviderException ex) {
    return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, ex.getCode());
  }


  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<Response> handleConflictException(ConflictException ex) {
    return buildErrorResponse(ex, HttpStatus.CONFLICT, ex.getCode());
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<Response> handleBadRequestException(BadRequestException ex) {
    return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, ex.getCode());
  }



}
