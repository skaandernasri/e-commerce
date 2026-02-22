package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "variant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VariantEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne
  @JoinColumn(name = "product_id", nullable = false)
  private ProduitEntity produit;
  private String color;
  private String size;
  private Long quantity;

  @Override
  public String toString() {
    return "VariantEntity{" + "id=" + id + ", color='" + color + '\'' + ", size='" + size + '\''
        + ", quantity=" + quantity + '}';
  }
}
