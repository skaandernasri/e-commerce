package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tn.temporise.domain.model.RefundMethod;
import tn.temporise.domain.model.StatusContact;
import tn.temporise.domain.model.TypeContact;

import java.time.LocalDateTime;

@Table(name = "contact")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ContactEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String subject;

  @Column(columnDefinition = "TEXT")
  private String message;

  private String email;

  @Enumerated(EnumType.STRING)
  private TypeContact type;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_contact")
  private StatusContact statusContact;

  @Enumerated(EnumType.STRING)
  @Column(name = "refund_method")
  private RefundMethod refundMethod;

  @Column(name = "is_refunded")
  private Boolean isRefunded = false;

  @Column(name = "refunded_at")
  private LocalDateTime refundedAt;
  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private UtilisateurEntity user;

  @ManyToOne
  @JoinColumn(name = "commande_id")
  private CommandeEntity commande;

  @Override
  public String toString() {
    return "ContactEntity{" + "id=" + id + ", subject='" + subject + '\'' + ", message='" + message
        + '\'' + ", type=" + type + ", statusContact=" + statusContact + ", refundMethod="
        + refundMethod + ", isRefunded=" + isRefunded + ", refundedAt=" + refundedAt
        + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + '}';
  }
}
