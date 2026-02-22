package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.temporise.domain.model.TypeAdresse;
import java.util.Set;

@Table(name = "adresse")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdresseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String ligne1;
  private String ligne2;
  private String ville;
  private String pays;
  private String codePostal;
  @Enumerated(EnumType.STRING)
  private TypeAdresse type;
  @ManyToOne
  private UtilisateurEntity utilisateur;
  @OneToMany(mappedBy = "adresseLivraison")
  private Set<CommandeEntity> commandesLivraison;
  @OneToMany(mappedBy = "adresseFacturation")
  private Set<CommandeEntity> commandesFacturation;



}
