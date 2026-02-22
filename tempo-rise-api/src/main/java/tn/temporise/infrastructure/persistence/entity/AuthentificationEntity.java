package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.temporise.domain.model.TypeAuthentification;

@Table(name = "authentification")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthentificationEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String password;
  private String providerId;
  private String refreshToken;
  @Enumerated(EnumType.STRING)
  private TypeAuthentification type;
  @ManyToOne()
  @JoinColumn(name = "user_id", nullable = false)
  private UtilisateurEntity user; // Just a reference without @ManyToOne

  @Override
  public String toString() {
    return "Authentification{" + "id=" + id + ", motDePasse='" + password + '\'' + ", providerId='"
        + providerId + '\'' + ", type=" + type + ", user=" + user + '}';
  }
}
