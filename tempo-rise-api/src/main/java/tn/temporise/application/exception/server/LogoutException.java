package tn.temporise.application.exception.server;

import lombok.Getter;

@Getter
public class LogoutException extends RuntimeException {
  private final String code;

  public LogoutException(String message, String code) {
    super(message);
    this.code = code;
  }
}
