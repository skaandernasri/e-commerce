package tn.temporise.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "google.recaptcha")
@Getter
@Setter
public class CaptchaSettings {
  private String url;
  private String secret;
  private String site;
}
