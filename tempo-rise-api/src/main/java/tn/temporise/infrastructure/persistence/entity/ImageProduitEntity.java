package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "image_produit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageProduitEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne
  @JoinColumn(name = "produit_id", nullable = false)
  private ProduitEntity produit;
  private String url;
  private byte[] contenu;

  public ImageProduitEntity(ProduitEntity produit) {
    this.produit = produit;
  }

  @Override
  public String toString() {
    return "ImageProduitEntity{" + "id=" + id + '}';
  }
}
