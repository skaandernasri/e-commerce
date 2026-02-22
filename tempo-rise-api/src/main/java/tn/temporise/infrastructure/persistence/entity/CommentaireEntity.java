package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "commentaire")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentaireEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String contenu;
  private Date datePublication;

  @ManyToOne
  @JoinColumn(name = "auteur_id", nullable = false)
  private UtilisateurEntity user;

  @ManyToOne
  @JoinColumn(name = "blogpost_id", nullable = false)
  private BlogPostEntity blogPost;
}
