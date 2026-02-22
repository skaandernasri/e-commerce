package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_notification")
@Getter
@Setter
public class UserNotifEntity {
  @EmbeddedId
  private UserNotifId id;

  @ManyToOne
  @MapsId("user_id")
  @JoinColumn(name = "user_id")
  private UtilisateurEntity user;

  @ManyToOne
  @MapsId("notification_id")
  @JoinColumn(name = "notification_id")
  private NotifEntity notif;
  @Column(name = "is_read")
  private boolean read;
  @Column(name = "read_at")
  private LocalDateTime readAt;

  @Override
  public String toString() {
    return "UserNotifEntity{" + "id=" + id + ", user=" + user + ", notification=" + notif
        + ", isRead=" + read + ", readAt=" + readAt + '}';
  }
}
