package tn.temporise.infrastructure.client.meta;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import tn.temporise.domain.model.MetaPixelEventRequest;

@FeignClient(name = "metaClient", url = "https://graph.facebook.com/v24.0")
public interface SendEventsApi {
  @PostMapping(value = "/{PIXEL_ID}/events", consumes = "application/json")
  void sendEvents(@PathVariable("PIXEL_ID") String pixelId,
      @RequestParam("access_token") String accessToken, @RequestBody MetaPixelEventRequest data);
}
