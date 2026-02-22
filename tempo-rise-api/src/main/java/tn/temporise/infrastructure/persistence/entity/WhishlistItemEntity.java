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
@Table(name = "whishlist_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class WhishlistItemEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "whishlist_id")
  private WhishlistEntity whishlist;

  @ManyToOne
  @JoinColumn(name = "produit_id")
  private ProduitEntity produit;

  @Column(name = "added_at", nullable = false, updatable = false)
  @CreatedDate
  private LocalDateTime addedAt;
}
