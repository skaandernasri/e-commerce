package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Table(name = "categorie")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CategorieEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nom;

  private String description;

  @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL)
  private List<ProduitEntity> produits = new ArrayList<>();

  public CategorieEntity(Long id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "CategorieEntity{" + "nom='" + nom + '\'' + ", id=" + id + ", description='"
        + description + '\'' + '}';
  }
}

