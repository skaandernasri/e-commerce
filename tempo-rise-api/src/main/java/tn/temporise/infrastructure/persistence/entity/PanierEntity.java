package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "panier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PanierEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToMany(mappedBy = "panier", cascade = CascadeType.ALL)
  private Set<PanierVariantEntity> panierProduit = new HashSet<>();
  @OneToOne
  @JoinColumn(name = "utilisateur_id", nullable = false)
  private UtilisateurEntity utilisateur;

  @Override
  public String toString() {
    return "PanierEntity{" + "id=" + id + ", utilisateur=" + utilisateur + '}';
  }
}
