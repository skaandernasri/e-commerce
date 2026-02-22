package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "facture")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FactureEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;


  @OneToOne
  @JoinColumn(name = "commande_id", nullable = false)
  private CommandeEntity commande;
  @Column(name = "date_emission", nullable = false, updatable = false)
  @CreatedDate
  private LocalDateTime dateEmission;
  private Double total;
}
