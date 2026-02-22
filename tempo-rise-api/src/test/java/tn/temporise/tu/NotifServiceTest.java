package tn.temporise.tu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.mapper.NotifMapper;
import tn.temporise.application.service.NotifService;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.NotifRepo;
import tn.temporise.domain.port.UserNotifRepo;
import tn.temporise.domain.port.UserRepo;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotifServiceTest {

  @Mock
  private NotifRepo notifRepo;
  @Mock
  private NotifMapper notifMapper;
  @Mock
  private ExceptionFactory exceptionFactory;
  @Mock
  private SimpMessagingTemplate simpMessagingTemplate;
  @Mock
  private UserNotifRepo userNotifRepo;
  @Mock
  private UserRepo userRepo;

  @InjectMocks
  private NotifService notifService;

  private UtilisateurModel adminUser;
  private UtilisateurModel normalUser;
  private Commande commande;
  private Notif generalNotif;
  private Notif specificNotif;

  @BeforeEach
  void setUp() {
    adminUser = UtilisateurModel.builder().id(1L).email("admin@example.com")
        .roles(Set.of(Role.ADMIN)).build();
    normalUser = UtilisateurModel.builder().id(2L).email("user@example.com")
        .roles(Set.of(Role.CLIENT)).build();

    commande = Commande.builder().id(10L).user(normalUser).build();

    generalNotif = Notif.builder().id(100L).title("General").message("General msg").build();
    specificNotif = Notif.builder().id(101L).title("Specific").message("Specific msg").build();
  }

  @Test
    void testNotifyOrder_Success_AdminsExist() {
        when(userRepo.findById(2L)).thenReturn(normalUser);
        when(userRepo.findByRole(Role.ADMIN)).thenReturn(List.of(adminUser));
        when(notifRepo.save(any(Notif.class))).thenReturn(generalNotif, specificNotif);
        when(userNotifRepo.countByUserIdAndIsRead(anyLong(), eq(false))).thenReturn(3L);
        when(notifMapper.modelToResponse(any(Notif.class)))
                .thenReturn(new NotificationResponse());

        notifService.notifyOrder(commande);

        verify(userNotifRepo, times(2)).save(any());
        verify(simpMessagingTemplate, atLeastOnce()).convertAndSendToUser(any(), any(), any());
        verify(notifRepo, times(2)).save(any());
    }

  @Test
    void testNotifyOrder_WithNullCommande_ThrowsBadRequest() {
        when(exceptionFactory.badRequest("badrequest.invalid_input"))
                .thenThrow(new BadRequestException("badrequest.invalid_input", "Invalid input"));

        assertThrows(BadRequestException.class, () -> notifService.notifyOrder(null));
    }

  @Test
  void testNotifyPromotion_Success() {
    Produit produit = Produit.builder().id(1L).nom("Test Product").build();
    Promotion promo = Promotion.builder().produit(produit).reduction(30.0).build();

    when(notifRepo.save(any(Notif.class))).thenReturn(generalNotif);
    when(userRepo.findAll()).thenReturn(List.of(adminUser, normalUser));
    when(userNotifRepo.countByUserIdAndIsRead(anyLong(), eq(false))).thenReturn(2L);
    when(notifMapper.modelToResponse(any(Notif.class))).thenReturn(new NotificationResponse());

    notifService.notifyPromotion(promo);

    verify(userNotifRepo, times(2)).save(any());
    verify(simpMessagingTemplate, times(2)).convertAndSendToUser(any(), eq("/queue"), any());
  }

  @Test
    void testNotifyPromotion_NullPromotion_ThrowsBadRequest() {
        when(exceptionFactory.badRequest("badrequest.invalid_input"))
                .thenThrow(new BadRequestException("badrequest.invalid_input", "Invalid input"));

        assertThrows(BadRequestException.class, () -> notifService.notifyPromotion(null));
    }
}
