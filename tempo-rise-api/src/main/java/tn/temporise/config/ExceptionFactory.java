package tn.temporise.config;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
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

@Component
@RequiredArgsConstructor
public class ExceptionFactory {
  private final ExceptionMessageUtil messageUtil;

  public PaymentException paymentException(String messageKey, String message) {
    String fullMessage = messageUtil.getMessage(messageKey);
    String[] parts = fullMessage.split("=", 2);
    return new PaymentException(parts[1] + message, parts[0]);
  }

  public CaptchaException captchaException(String messageKey) {
    String fullMessage = messageUtil.getMessage(messageKey);
    String[] parts = fullMessage.split("=", 2);
    return new CaptchaException(parts[1], parts[0]);
  }

  public ProviderException providerException(String messageKey) {
    String fullMessage = messageUtil.getMessage(messageKey);
    String[] parts = fullMessage.split("=", 2);
    return new ProviderException(parts[1], parts[0]);
  }


  // BadRequestException
  public BadRequestException badRequest(String messageKey) {
    String fullMessage = messageUtil.getMessage(messageKey);
    String[] parts = fullMessage.split("=", 2);
    return new BadRequestException(parts[1], parts[0]);
  }



  // ConflictException
  public ConflictException conflict(String messageKey) {
    String fullMessage = messageUtil.getMessage(messageKey);
    String[] parts = fullMessage.split("=", 2);
    return new ConflictException(parts[1], parts[0]);
  }



  // InternalServerErrorException
  public InternalServerErrorException internalServerError(String messageKey, String message) {
    String fullMessage = messageUtil.getMessage(messageKey);
    String[] parts = fullMessage.split("=", 2);
    return new InternalServerErrorException(parts[1] + message, parts[0]);
  }



  // LogoutException
  public LogoutException logout(String messageKey) {
    String fullMessage = messageUtil.getMessage(messageKey);
    String[] parts = fullMessage.split("=", 2);
    return new LogoutException(parts[1], parts[0]);
  }

  // NonLocalProviderException
  public ProviderException nonLocalProvider(String messageKey) {
    String fullMessage = messageUtil.getMessage(messageKey);
    String[] parts = fullMessage.split("=", 2);
    return new ProviderException(parts[1], parts[0]);
  }

  // NotFoundException (if you have one)
  public NotFoundException notFound(String messageKey) {
    String fullMessage = messageUtil.getMessage(messageKey);
    String[] parts = fullMessage.split("=", 2);
    return new NotFoundException(parts[1], parts[0]);
  }
  // Add these methods to your existing ExceptionFactory class

  // PasswordException
  public PasswordException password(String messageKey) {
    String fullMessage = messageUtil.getMessage(messageKey);
    String[] parts = fullMessage.split("=", 2);
    return new PasswordException(parts[1], parts[0]);
  }

  // RegistrationException
  public RegistrationException registration(String messageKey) {
    String fullMessage = messageUtil.getMessage(messageKey);
    String[] parts = fullMessage.split("=", 2);
    return new RegistrationException(parts[1], parts[0]);
  }

  // UnauthorizedException
  public UnauthorizedException unauthorized(String messageKey) {
    String fullMessage = messageUtil.getMessage(messageKey);
    String[] parts = fullMessage.split("=", 2);
    return new UnauthorizedException(parts[1], parts[0]);
  }

}
