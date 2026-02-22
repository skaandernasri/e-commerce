package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "utilisateuranonyme")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
// you need to extends UtilisateurEntity
public class UtilisateurAnonymeEntity extends UtilisateurEntity {

  @Column(name = "session_token", unique = true, nullable = false)
  private UUID sessionToken;
  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Override
  public String toString() {
    return "UtilisateurAnonyme{" + ", sessionToken=" + sessionToken + ", createdAt=" + createdAt
        + '}';
  }
}
