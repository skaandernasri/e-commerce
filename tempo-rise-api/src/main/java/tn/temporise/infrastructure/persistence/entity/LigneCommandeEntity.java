package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ligne_commande")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LigneCommandeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private long quantite;
  @Column(name = "prix_total")
  private double prixTotal;
  @ManyToOne
  @JoinColumn(name = "variant_id", nullable = false)
  private VariantEntity variant;
  @ManyToOne
  @JoinColumn(name = "commande_id", nullable = false)
  private CommandeEntity commande;

  @Override
  public String toString() {
    return "LigneCommandeEntity{" + "id=" + id + ", quantite=" + quantite + ", prixTotal="
        + prixTotal + ", variant=" + variant + '}';
  }
}

