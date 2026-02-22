package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tn.temporise.domain.model.TypePage;
import tn.temporise.domain.model.TypeSection;

import java.time.LocalDateTime;

@Entity
@Table(name = "section")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SectionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String titre;

  @Enumerated(EnumType.STRING)
  private TypeSection type;

  @Enumerated(EnumType.STRING)
  private TypePage typePage;
  @Column(name = "image_url")
  private String imageUrl;

  @Column(name = "contenu_json", columnDefinition = "TEXT")
  private String contenuJson;

  @Column(name = "contenu_html", columnDefinition = "TEXT")
  private String contenuHtml;

  @Column(name = "is_active")
  private boolean active;
  @Column(name = "created_at", updatable = false)
  @CreatedDate
  private LocalDateTime createdAt;
  @Column(name = "updated_at", insertable = false)
  private LocalDateTime updatedAt;

  @ManyToOne
  @JoinColumn(name = "created_by")
  @CreatedBy
  private UtilisateurEntity createdBy;

  @ManyToOne
  @JoinColumn(name = "updated_by")
  private UtilisateurEntity updatedBy;


  @Override
  public String toString() {
    return "SectionEntity{" + "id=" + id + ", titre='" + titre + '\'' + ", type=" + type
        + ", contenuJson='" + contenuJson + '\'' + ", active=" + active + ", createdAt=" + createdAt
        + ", updatedAt=" + updatedAt + ", createdBy=" + createdBy + ", updatedBy=" + updatedBy
        + '}';
  }
}
