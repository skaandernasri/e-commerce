package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tn.temporise.domain.model.TypeAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "suivi_client")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SuiviClientEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne
  @JoinColumn(name = "utilisateur_id")
  private UtilisateurEntity utilisateur;
  @ManyToOne
  @JoinColumn(name = "produit_id", nullable = false)
  private ProduitEntity produit;
  @Enumerated(EnumType.STRING)
  private TypeAction typeAction;
  private double score;
  @CreatedDate
  @Column(nullable = false)
  private LocalDateTime date;
  @ManyToOne
  @JoinColumn(name = "utilisateur_anonyme_id")
  private UtilisateurAnonymeEntity utilisateurAnonyme;

  @Override
  public String toString() {
    return "SuiviClientEntity{" + "id=" + id + ", utilisateur=" + utilisateur + ", produit="
        + produit + ", typeAction=" + typeAction + ", score=" + score + ", date=" + date
        + ", utilisateurAnonyme=" + utilisateurAnonyme + '}';
  }
}
