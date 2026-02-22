package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tn.temporise.domain.model.NewsLetterStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "news_letter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NewsLetterEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String email;
  @CreatedDate
  private LocalDateTime createdAt;
  private LocalDateTime subscribedAt;
  private LocalDateTime unsubscribedAt;
  private NewsLetterStatus status;
  private UUID confirmationToken;
  private LocalDateTime confirmationTokenExpiresAt;
  private UUID unsubsriptionToken;
  private LocalDateTime unsubsriptionTokenExpiresAt;

  @Override
  public String toString() {
    return "NewsLetterEntity{" + "id=" + id + ", email='" + email + '\'' + ", createdAt="
        + createdAt + ", subscribedAt=" + subscribedAt + ", unsubscribedAt=" + unsubscribedAt
        + ", status=" + status + ", confirmationToken=" + confirmationToken
        + ", confirmationTokenExpiresAt=" + confirmationTokenExpiresAt + ", unsubsriptionToken="
        + unsubsriptionToken + ", unsubsriptionTokenExpiresAt=" + unsubsriptionTokenExpiresAt + '}';
  }
}
