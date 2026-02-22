package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tn.temporise.domain.model.NotifType;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NotifEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String title;
  private String message;
  @Enumerated(EnumType.STRING)
  private NotifType type;
  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
  @Column(name = "redirect_url")
  private String redirectUrl;

  @Override
  public String toString() {
    return "NotifEntity{" + "id=" + id + ", title='" + title + '\'' + ", message='" + message + '\''
        + ", type=" + type + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
        + ", redirectUrl='" + redirectUrl + '\'' + '}';
  }
}
