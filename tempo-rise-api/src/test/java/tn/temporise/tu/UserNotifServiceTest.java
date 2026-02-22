package tn.temporise.tu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Page;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.*;
import tn.temporise.application.service.UserNotifService;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.UserNotifRepo;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserNotifServiceTest {

  @InjectMocks
  private UserNotifService userNotifService;

  @Mock
  private ExceptionFactory exceptionFactory;

  @Mock
  private UserNotifRepo userNotifRepo;

  @Mock
  private UserNotifMapper userNotifMapper;

  @Mock
  private DateMapper dateMapper;

  @Mock
  private NotifMapper notifMapper;

  @Mock
  private UserMapper userMapper;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetPaginatedUserNotifs() {
    Long userId = 1L;
    int page = 1;
    int size = 2;

    List<UserNotif> userNotifList =
        List.of(UserNotif.builder().user(UtilisateurModel.builder().id(1L).build())
            .notif(Notif.builder().id(1L).build()).build());
    Page<UserNotif> userNotifPage = new PageImpl<>(userNotifList);
    UserNotifResponse response = new UserNotifResponse();

    when(userNotifRepo.findAll(eq(userId), any(Pageable.class))).thenReturn(userNotifPage);
    when(userNotifMapper.modelToResponse(eq(userNotifList), any(), any(), any()))
        .thenReturn(response);
    when(userNotifRepo.countByUserIdAndIsRead(userId, false)).thenReturn(5L);

    UserNotifResponse result = userNotifService.getPaginatedUserNotifs(userId, page, size);

    assertEquals(5L, result.getUnreadCount());
    verify(userNotifRepo).findAll(eq(userId), any(Pageable.class));
    verify(userNotifMapper).modelToResponse(any(), any(), any(), any());
  }

  @Test
  void testMarkAllAsRead() throws NotFoundException {
    Long userId = 1L;
    NotificationResponse notif1 = new NotificationResponse();
    notif1.setId(10L);

    NotificationResponse notif2 = new NotificationResponse();
    notif2.setId(20L);

    List<NotificationResponse> notifs = List.of(notif1, notif2);

    when(userNotifRepo.countByUserIdAndIsRead(userId, false)).thenReturn(2L);

    MarkAllAsRead200Response response = userNotifService.markAllAsRead(userId, notifs);

    assertEquals(2L, response.getUnreadCount());
    verify(userNotifRepo).markAsRead(eq(userId), eq(List.of(10L, 20L)), eq(false));
  }
}
