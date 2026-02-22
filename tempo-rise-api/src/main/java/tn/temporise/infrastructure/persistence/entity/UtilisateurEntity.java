package tn.temporise.infrastructure.persistence.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import tn.temporise.domain.model.Genre;
import tn.temporise.domain.model.Role;
import tn.temporise.domain.model.UserType;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "utilisateur")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UtilisateurEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "nom")
  private String nom;
  @Column(name = "prenom")
  private String prenom;
  @Column(name = "téléphone")
  private String telephone;
  @Enumerated(EnumType.STRING)
  private Genre genre;
  private int loyalty_group;
  @CreatedDate
  @Column(name = "date_naissance", nullable = false)
  private Date date_naissance;
  private String email;
  @Column(name = "image_url")
  private String imageUrl;
  @Column(name = "type_utilisateur")
  @Enumerated(EnumType.STRING)
  private UserType userType;
  @Transient
  private String password;
  @Column(name = "image")
  private byte[] image;
  private boolean isverified;
  private String activation_token;
  private String resetpasswordtoken;
  private LocalDateTime resetpasswordexpiresat;
  private LocalDateTime activationtokenexpiresat;
  @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER) // it was EAGER
  @CollectionTable(name = "utilisateur_role", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "role", nullable = false)
  @Enumerated(EnumType.STRING)
  private Set<Role> roles = new HashSet<>();
  @ManyToMany
  @JoinTable(name = "user_notification", joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "notification_id"))
  private Set<NotifEntity> notifications = new HashSet<>();

  public UtilisateurEntity(String email, String nom, Role role) {
    this.email = email;
    this.nom = nom;
    this.roles.add(role);

  }

  public UtilisateurEntity(Long id) {
    this.id = id;

  }


  public UtilisateurEntity(String email, Role role) {
    this.email = email;
    this.roles.add(role);
  }

  public UtilisateurEntity(String email, Role role, boolean isverified) {
    this.email = email;
    this.roles.add(role);
    this.isverified = isverified;
  }


  @Override
  public String toString() {
    return "UtilisateurEntity{" + "id=" + id + ", prenom='" + prenom + '\'' + ", nom='" + nom + '\''
        + ", email='" + email + '\'' + ", is_verified=" + isverified + ", activation_token='"
        + activation_token + '\'' + ", roles=" + roles + ", date_naissance=" + date_naissance + '\''
        + ", loyalty_group=" + loyalty_group + '}';
  }
}
