package tn.temporise.infrastructure.client.recaptcha;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import tn.temporise.domain.model.RecaptchaResponse;

@FeignClient(name = "recaptchaClient", url = "https://www.google.com")
public interface RecaptchaClient {

  @PostMapping(value = "/recaptcha/api/siteverify",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  RecaptchaResponse verify(@RequestBody MultiValueMap<String, String> body);
}
