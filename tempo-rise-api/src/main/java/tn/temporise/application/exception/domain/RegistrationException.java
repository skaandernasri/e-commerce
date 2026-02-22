package tn.temporise.application.exception.domain;

import lombok.Getter;

@Getter
public class RegistrationException extends RuntimeException {
  private final String code;

  public RegistrationException(String message, String code) {
    super(message);
    this.code = code;
  }
}
