package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.MetaPixelService;
import tn.temporise.domain.model.MetaPixelEventRequest;
import tn.temporise.domain.model.Response;
import tn.temporise.infrastructure.api.MetaPixelApi;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MetaPixelController implements MetaPixelApi {
  private final MetaPixelService metaPixelService;


  @Override
  public ResponseEntity<Resource> _downloadCatalogueCsv() throws Exception {
    return ResponseEntity.ok(metaPixelService.generateCatalogueCsv());
  }

  @Override
  public ResponseEntity<Response> _sendMetaPixelEvent(MetaPixelEventRequest metaPixelEventRequest)
      throws Exception {
    return ResponseEntity.ok(metaPixelService.sendMetaPixelEvent(metaPixelEventRequest));
  }
}
