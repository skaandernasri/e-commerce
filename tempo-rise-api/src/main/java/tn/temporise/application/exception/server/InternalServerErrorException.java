package tn.temporise.application.exception.server;

import lombok.Getter;

@Getter
public class InternalServerErrorException extends RuntimeException {
  private final String code;

  public InternalServerErrorException(String message, String code) {
    super(message);
    this.code = code;
  }
}

