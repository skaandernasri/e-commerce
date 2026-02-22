package tn.temporise.infrastructure.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.UserNotifService;
import tn.temporise.domain.model.MarkAllAsRead200Response;
import tn.temporise.domain.model.NotificationResponse;
import tn.temporise.domain.model.UserNotifResponse;
import tn.temporise.infrastructure.api.NotificationApi;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserNotifController implements NotificationApi {
  private final UserNotifService userNotifService;

  @PreAuthorize("@userNotifService.isNotifOwnerByUserId(#userId)")
  @Override
  public ResponseEntity<UserNotifResponse> _getNotifs(Long userId, Integer page, Integer size)
      throws Exception {
    return ResponseEntity.ok(userNotifService.getPaginatedUserNotifs(userId, page, size));
  }


  @PreAuthorize("@userNotifService.isNotifOwnerByUserId(#userId)")
  @Override
  public ResponseEntity<MarkAllAsRead200Response> _markAllAsRead(Long userId,
      @Valid @RequestBody List<@Valid NotificationResponse> notificationResponse) throws Exception {
    return ResponseEntity.ok(userNotifService.markAllAsRead(userId, notificationResponse));
  }
}
