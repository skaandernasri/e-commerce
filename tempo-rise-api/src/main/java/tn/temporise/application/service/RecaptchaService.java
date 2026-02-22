package tn.temporise.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tn.temporise.config.CaptchaSettings;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.RecaptchaResponse;
import tn.temporise.infrastructure.client.recaptcha.RecaptchaClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class RecaptchaService {

  private final RecaptchaClient recaptchaClient;
  private final CaptchaSettings captchaSettings;
  private final ExceptionFactory exceptionFactory;

  public void verifyToken(String token) {
    String secretValue = captchaSettings.getSecret();

    // Check if secretValue is a file path
    Path path = Paths.get(secretValue);
    if (Files.exists(path)) {
      try {
        // Read the secret from the file
        secretValue = Files.readString(path).trim();
        System.out.println("Secret is a file. Content: " + secretValue);
      } catch (IOException e) {
        throw new RuntimeException("Could not read reCAPTCHA secret file", e);
      }
    } else {
      // Treat as plain string
      System.out.println("Secret is a string: " + secretValue);
    }

    // Now use secretValue for verification
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("secret", secretValue);
    body.add("response", token);

    RecaptchaResponse response = recaptchaClient.verify(body);
    if (response == null || !response.getSuccess() || response.getScore() < 0.5) {
      throw exceptionFactory.captchaException("captcha_failed");
    }
  }
}
