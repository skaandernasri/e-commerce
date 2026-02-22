package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;

@Entity
@Table(name = "produit")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProduitEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nom;

  private String description;

  private double prix;

  private String marque;

  private Boolean actif;

  private String composition;

  private String guide;

  private String faq;


  @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<PromotionEntity> promotions;
  @ManyToOne
  @JoinColumn(name = "categorie_id", nullable = false)
  private CategorieEntity categorie;
  @ManyToMany(mappedBy = "produits", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
  private Set<HistoriqueCommandeEntity> historiqueCommandeEntities;
  @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<ImageProduitEntity> imageProduits;
  @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<AvisEntity> avis;
  @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Set<VariantEntity> variants;

  @Override
  public String toString() {
    return "ProduitEntity{" + "id=" + id + ", nom='" + nom + '\'' + ", description='" + description
        + '\'' + ", prix=" + prix + ", marque='" + marque + '\'' + ", composition='" + composition
        + '\'' + ", guide='" + guide + '\'' + ", faq='" + faq + '\'' + ", promotions=" + promotions
        + ", categorie=" + categorie + ", historiqueCommandeEntities=" + historiqueCommandeEntities
        + ", imageProduits=" + imageProduits + ", avis=" + avis + ", variants=" + variants + '}';
  }
}

