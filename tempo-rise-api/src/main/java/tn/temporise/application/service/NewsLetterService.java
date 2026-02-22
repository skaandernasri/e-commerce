package tn.temporise.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.temporise.application.mapper.NewsLetterMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.port.NewsLetterRepo;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsLetterService {
  private final ExceptionFactory exceptionFactory;
  private final NewsLetterRepo newsLetterRepo;
  private final NewsLetterMapper newsLetterMapper;

  // public Response subscribe(SubscribeRequest subscribeRequest) {
  // try {
  // if (subscribeRequest == null || subscribeRequest.getEmail() == null) {
  // throw exceptionFactory.badRequest("badrequest.invalid_request");
  // }
  // try{
  // newsLetterRepo.findByEmail(subscribeRequest.getEmail());
  // throw exceptionFactory.conflict("conflict.email_exists");
  // }catch (NotFoundException ignore){
  // NewsLetter newsLetter = NewsLetter.builder()
  // .email(subscribeRequest.getEmail())
  // .build();
  // newsLetterRepo.save(newsLetter);
  // }
  // } catch (Exception e) {
  // throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
  // }
  // }
}
