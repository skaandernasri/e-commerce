package tn.temporise.application.exception.client;

import lombok.Getter;

@Getter
public class PasswordException extends RuntimeException {
  private final String code;

  public PasswordException(String message, String code) {
    super(message);
    this.code = code;
  }
}
