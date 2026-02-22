package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "configuration_global")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationGlobalEntity {
  @Id
  private long id;
  @Column(name = "valeur_livraison")
  private Float valeurLivraison;
  @Column(name = "seuil_livraison_gratuite")
  private Float seuilLivraisonGratuite;
}
