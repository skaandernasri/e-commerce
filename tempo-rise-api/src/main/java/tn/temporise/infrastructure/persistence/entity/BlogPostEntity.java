package tn.temporise.infrastructure.persistence.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.List;


@Table(name = "blogpost")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BlogPostEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String titre;
  private String contenu;
  // Entité corrigée
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private BlogPostStatus status = BlogPostStatus.BROUILLON;

  @Column(name = "date_publication")
  private LocalDateTime date_publication;

  @Override
  public String toString() {
    return "BlogPostEntity{" + "id=" + id + ", titre='" + titre + '\'' + ", contenu='" + contenu
        + '\'' + ", status=" + status + ", date_publication=" + date_publication + ", commentaires="
        + commentaires + ", images=" + images + '}';
  }

  @ManyToOne
  @JoinColumn(name = "auteur_id", nullable = false)
  private UtilisateurEntity auteur;

  @OneToMany(mappedBy = "blogPost", cascade = CascadeType.ALL)
  private List<CommentaireEntity> commentaires;

  @OneToMany(mappedBy = "blogPost", cascade = CascadeType.ALL)
  private List<ImageBlogPostEntity> images;

  public void setId(Long id) {
    this.id = id;
  }



}
