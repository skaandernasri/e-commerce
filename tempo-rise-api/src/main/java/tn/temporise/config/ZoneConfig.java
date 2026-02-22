package tn.temporise.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.*;

@Configuration
@ConfigurationProperties(prefix = "spring.app")
@Getter
@Setter
public class ZoneConfig {
  private String timezone;

  public ZonedDateTime toZonedDateTime(LocalDateTime utcLocalDateTime) {
    if (utcLocalDateTime == null) {
      return null;
    }
    // Treat the LocalDateTime as UTC first
    ZonedDateTime utcZoned = utcLocalDateTime.atZone(ZoneOffset.UTC);
    // Then convert to target zone
    return utcZoned.withZoneSameInstant(ZoneId.of(timezone));
  }

  @PostConstruct
  public void init() {
    if (timezone == null || timezone.isEmpty()) {
      timezone = "UTC";
    }
    // Optional: validate the timezone ID
    try {
      ZoneId.of(timezone);
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("Invalid timezone configured: " + timezone, e);
    }
  }
}
