package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.NewsLetterService;
import tn.temporise.domain.model.Response;
import tn.temporise.domain.model.SubscribeRequest;
import tn.temporise.infrastructure.api.NewsLetterApi;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
public class NewsLetterController implements NewsLetterApi {
  private final NewsLetterService newsLetterService;

  @Override
  public ResponseEntity<Response> _confirmSubscription(UUID token) throws Exception {
    return null;
  }

  @Override
  public ResponseEntity<Response> _subscribe(SubscribeRequest subscribeRequest) throws Exception {
    return null;
  }

  @Override
  public ResponseEntity<Response> _unSubscribe(UUID token) throws Exception {
    return null;
  }
}
