package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "retour_produit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RetourProduitEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String raisonRetour;
  @CreatedDate
  @Column(name = "date_retour", nullable = false, updatable = false)
  private LocalDateTime dateRetour;

  @ManyToOne
  @JoinColumn(name = "variant_id", nullable = false)
  private VariantEntity variant;
  @ManyToOne
  @JoinColumn(name = "utilisateur_id", nullable = false)
  private UtilisateurEntity utilisateur;
}
