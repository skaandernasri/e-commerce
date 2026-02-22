package tn.temporise.application.exception.client;

import lombok.Getter;

@Getter
public class ConflictException extends RuntimeException {
  private final String code;

  public ConflictException(String message, String code) {
    super(message);
    this.code = code;
  }
}
