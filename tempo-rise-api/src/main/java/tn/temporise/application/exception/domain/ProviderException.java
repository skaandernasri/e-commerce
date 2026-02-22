package tn.temporise.application.exception.domain;

import lombok.Getter;

@Getter
public class ProviderException extends RuntimeException {
  private final String code;

  public ProviderException(String message, String code) {
    super(message);
    this.code = code;
  }
}
