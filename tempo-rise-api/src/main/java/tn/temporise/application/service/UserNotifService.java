package tn.temporise.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.DateMapper;
import tn.temporise.application.mapper.NotifMapper;
import tn.temporise.application.mapper.UserMapper;
import tn.temporise.application.mapper.UserNotifMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.UserNotifRepo;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserNotifService {
  private final ExceptionFactory exceptionFactory;
  private final UserNotifRepo userNotifRepo;
  private final UserNotifMapper userNotifMapper;
  private final DateMapper dateMapper;
  private final NotifMapper notifMapper;
  private final UserMapper userMapper;

  public UserNotifResponse getPaginatedUserNotifs(Long userId, int page, int size) {
    try {
      page = Math.max(page - 1, 0);
      Page<UserNotif> userNotifPage = userNotifRepo.findAll(userId,
          PageRequest.of(page, size, Sort.by("notif.updatedAt").descending()));
      log.info("userNotifPage: {}", userNotifPage.getContent());
      if (userNotifPage.isEmpty()) {
        return new UserNotifResponse();
      }
      UserNotifResponse userNotifResponse = userNotifMapper
          .modelToResponse(userNotifPage.getContent(), notifMapper, dateMapper, userMapper);

      userNotifResponse.setUnreadCount(userNotifRepo.countByUserIdAndIsRead(userId, false));
      userNotifResponse.setTotalElements(userNotifPage.getTotalElements());
      userNotifResponse.setTotalPages(userNotifPage.getTotalPages());
      userNotifResponse.setCurrentPage(userNotifPage.getNumber() + 1);
      userNotifResponse.setSize(userNotifPage.getSize());
      log.info("userNotifResponse: {}", userNotifResponse.getNotifs());
      return userNotifResponse;
    } catch (NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }

  }

  public MarkAllAsRead200Response markAllAsRead(Long userId,
      List<NotificationResponse> notificationResponses) throws NotFoundException {
    try {
      List<Long> notifIds =
          notificationResponses.stream().map(NotificationResponse::getId).toList();
      userNotifRepo.markAsRead(userId, notifIds, false);
      MarkAllAsRead200Response markAllAsRead200Response = new MarkAllAsRead200Response();
      markAllAsRead200Response.setUnreadCount(userNotifRepo.countByUserIdAndIsRead(userId, false));
      return markAllAsRead200Response;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public boolean isNotifOwnerByUserId(Long userId) throws NotFoundException {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
        || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {

      return false;
    }
    return userId.equals(userDetails.id());
  }
}
