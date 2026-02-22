package tn.temporise.application.exception.external;

import lombok.Getter;

@Getter
public class PaymentException extends RuntimeException {
  private final String code;

  public PaymentException(String message, String code) {
    super(message);
    this.code = code;
  }
}
