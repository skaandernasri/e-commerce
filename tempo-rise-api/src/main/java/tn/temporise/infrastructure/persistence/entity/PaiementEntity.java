package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.temporise.domain.model.WebhookResponsePayment.StatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaiementEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "paiement_ref")
  private String paiementRef;
  @Column(nullable = false)
  private LocalDateTime date;
  @ManyToOne
  @JoinColumn(name = "commande_id", nullable = false)
  private CommandeEntity commande;
  @Enumerated(EnumType.STRING)
  @Column(name = "statut")
  private StatusEnum status;
  private String nom;
  private BigDecimal amount;
  private String type;


}
