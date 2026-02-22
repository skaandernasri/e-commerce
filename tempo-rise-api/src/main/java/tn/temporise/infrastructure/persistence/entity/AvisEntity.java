package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


import java.time.LocalDateTime;

@Entity
@Table(name = "avis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AvisEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private int note;
  private String commentaire;
  @Column(name = "date_publication", nullable = false, updatable = false)
  @CreatedDate
  private LocalDateTime datePublication;
  @ManyToOne
  @JoinColumn(name = "utilisateur_id", nullable = false)
  private UtilisateurEntity utilisateur;

  @ManyToOne
  @JoinColumn(name = "produit_id", nullable = false)
  private ProduitEntity produit;

  @Override
  public String toString() {
    return "AvisEntity{" + "id=" + id + ", note=" + note + ", commentaire='" + commentaire + '\''
        + ", datePublication=" + datePublication + ", utilisateur=" + utilisateur + '}';
  }
}
