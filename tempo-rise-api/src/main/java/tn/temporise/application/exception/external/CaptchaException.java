package tn.temporise.application.exception.external;

import lombok.Getter;

@Getter
public class CaptchaException extends RuntimeException {
  private final String code;

  public CaptchaException(String message, String code) {
    super(message);
    this.code = code;
  }
}
