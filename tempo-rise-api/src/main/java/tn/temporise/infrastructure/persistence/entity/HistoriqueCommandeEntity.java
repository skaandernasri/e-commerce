package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.temporise.domain.model.StatutCommande;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "historique_commande")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueCommandeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private LocalDateTime dateCommande;
  private StatutCommande statut;
  @ManyToMany
  @JoinTable(name = "historiquecommande_produit",
      joinColumns = @JoinColumn(name = "historiquecommande_id"),
      inverseJoinColumns = @JoinColumn(name = "produit_id"))
  Set<ProduitEntity> produits = new HashSet<>();
  @ManyToOne
  @JoinColumn(name = "utilisateur_id", nullable = false)
  private UtilisateurEntity user;
}
