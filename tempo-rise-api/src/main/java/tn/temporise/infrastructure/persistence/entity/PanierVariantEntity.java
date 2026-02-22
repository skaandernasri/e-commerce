package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter

@Entity
@Table(name = "panier_produit")
public class PanierVariantEntity {

  @EmbeddedId
  private PanierVariantId id = new PanierVariantId();
  @ManyToOne
  @MapsId("panier_id")
  @JoinColumn(name = "panier_id")
  private PanierEntity panier;

  @ManyToOne
  @MapsId("variant_id")
  @JoinColumn(name = "variant_id")
  private VariantEntity variant;

  private Long quantite;
  @Column(name = "expiration_date")
  private LocalDateTime expirationDate;

  public PanierVariantEntity() {

  }

  @Override
  public String toString() {
    return "PanierVariantId{" + "id=" + id.toString() + ", panier=" + panier.toString()
        + ", variant=" + variant.toString() + ", quantite=" + quantite + ", expirationDate="
        + expirationDate + '}';
  }
}
