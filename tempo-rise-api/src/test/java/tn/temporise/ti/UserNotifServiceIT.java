package tn.temporise.ti;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tn.temporise.application.service.UserNotifService;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.NotifRepo;
import tn.temporise.domain.port.UserNotifRepo;
import tn.temporise.infrastructure.persistence.entity.UserNotifId;

import static org.junit.jupiter.api.Assertions.*;

class UserNotifServiceIT extends BaseIntegrationTest {

  @Autowired
  private UserNotifService userNotifService;

  @Autowired
  private UserNotifRepo userNotifRepo;
  @Autowired
  private NotifRepo notifRepo;

  @Test
  void testGetPaginatedUserNotifs() {
    // Suppose que l'utilisateur avec ID 1L a déjà des notifications dans la BDD de test

    Notif notif = notifRepo.save(Notif.builder().title("Test title").message("Test message")
        .type(NotifType.NEW_ORDER).redirectUrl("http://example.com").build());
    userNotifRepo.save(UserNotif.builder().id(new UserNotifId(userId, notif.id())).notif(notif)
        .user(currentUser).build());
    UserNotifResponse response = userNotifService.getPaginatedUserNotifs(userId, 1, 10);

    assertNotNull(response);
    assertNotNull(response.getNotifs());
    assertTrue(response.getCurrentPage() >= 1);
  }
}
