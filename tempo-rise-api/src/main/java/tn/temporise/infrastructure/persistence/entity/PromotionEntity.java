package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.temporise.domain.model.PromotionType;

import java.time.LocalDateTime;

@Entity
@Table(name = "promotion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PromotionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String nom;
  private String description;
  private double reduction;
  private LocalDateTime dateDebut;
  private LocalDateTime dateFin;
  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  private PromotionType type;
  @ManyToOne
  @JoinColumn(name = "produit_id", nullable = false)
  private ProduitEntity produit;

  @Override
  public String toString() {
    return "PromotionEntity{" + "id=" + id + ", nom='" + nom + '\'' + ", description='"
        + description + '\'' + ", reduction=" + reduction + ", dateDebut=" + dateDebut
        + ", dateFin=" + dateFin + '}';
  }
}
