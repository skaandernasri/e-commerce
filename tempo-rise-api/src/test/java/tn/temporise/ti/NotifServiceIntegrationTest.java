package tn.temporise.ti;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.persistence.entity.UserNotifId;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NotifServiceIntegrationTest extends BaseIntegrationTest {


  @BeforeEach
  void setUpTestData() {
    // super.setUp();
  }

  @Test
  void testNotifyPromotionIntegration() {
    // Arrange
    Produit produit =
        Produit.builder().nom("Promo Product").description("Great deal").prix(100.0).build();
    produit = productRepo.save(produit);

    Promotion promotion = Promotion.builder().produit(produit).reduction(25.0)
        .dateDebut(LocalDateTime.now().minusDays(1)).dateFin(LocalDateTime.now().plusDays(1))
        .nom("New Promotion").build();

    // Act
    notifService.notifyPromotion(promotion);

    // Assert
    List<UserNotif> userNotifs = userNotifRepo
        .findAll(userId, PageRequest.of(0, 10, Sort.by("notif.updatedAt").descending())).stream()
        .toList();
    assertFalse(userNotifs.isEmpty(), "At least one user notification should be created");

    UserNotif notif = userNotifs.getFirst();
    assertNotNull(notif.notif(), "Notification entity should be saved");
    assertEquals("New Promotion", notif.notif().title());
    assertTrue(notif.notif().message().contains("Promo Product"));
  }

  @Test
  void testNotifyOrderIntegration() {
    // Arrange
    UtilisateurModel user = userRepo.findById(userId);
    assertNotNull(user, "User should exist");

    Commande commande = Commande.builder().user(user).id(101L)
        .modePaiement(ModePaiement.CARTE_BANCAIRE).statut(StatutCommande.EN_COURS).build();

    // Act
    notifService.notifyOrder(commande);

    // Assert
    List<UserNotif> userNotifs = userNotifRepo
        .findAll(userId, PageRequest.of(0, 10, Sort.by("notif.updatedAt").descending())).stream()
        .toList();
    assertFalse(userNotifs.isEmpty(), "At least one user notification should be saved");

    UserNotif anyNotif = userNotifs.getFirst();
    UserNotifId id = anyNotif.id();

    assertNotNull(anyNotif.notif(), "Notification should not be null");
    assertNotNull(id.getNotification_id(), "Notification ID should be present");
    assertNotNull(id.getUser_id(), "User ID should be present");
    assertEquals(userId, id.getUser_id(), "User ID should match the test user");

    assertTrue(anyNotif.notif().message().contains("order"), "Message should mention the order");
  }
}
