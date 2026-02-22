package tn.temporise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import tn.temporise.config.CaptchaSettings;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = "tn.temporise")
@EnableJpaRepositories(basePackages = "tn.temporise")
@EntityScan(basePackages = "tn.temporise.infrastructure.persistence.entity")
@EnableConfigurationProperties({CaptchaSettings.class})
@EnableScheduling
@EnableFeignClients(basePackages = "tn.temporise.infrastructure.client")
@EnableJpaAuditing
public class TempoRiseApiApplication {
  public static void main(String[] args) {
    SpringApplication.run(TempoRiseApiApplication.class, args);
  }

  @PostConstruct
  public void init() {
    // Set JVM default timezone to UTC
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }
}
