package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tn.temporise.domain.model.ModePaiement;
import tn.temporise.domain.model.StatutCommande;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "commande")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CommandeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "date_commande", nullable = false, updatable = false)
  @CreatedDate
  private LocalDateTime date;
  @Column(name = "total", nullable = false)
  private double total;
  private String nom;
  private String prenom;
  private String email;
  private String telephone;
  @Enumerated(EnumType.STRING)
  private StatutCommande statut;

  @ManyToOne
  @JoinColumn(name = "utilisateur_id", nullable = false)
  private UtilisateurEntity user;

  @Enumerated(EnumType.STRING)
  @Column(name = "mode_paiement", nullable = false)
  private ModePaiement modePaiement;

  @OneToOne
  @JoinColumn(name = "codePromo_id")
  private CodePromoEntity codePromo;

  @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL)
  private List<LigneCommandeEntity> lignesCommande = new ArrayList<>();

  @OneToOne(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
  private FactureEntity facture;

  @ManyToOne
  @JoinColumn(name = "adresse_livraison_id")
  private AdresseEntity adresseLivraison;

  @ManyToOne
  @JoinColumn(name = "adresse_facturation_id")
  private AdresseEntity adresseFacturation;

  @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL)
  private Set<PaiementEntity> paiements = new HashSet<>();

  @Override
  public String toString() {
    return "CommandeEntity{" + "id=" + id + ", date=" + date + ", statut=" + statut + ", user="
        + user + ", modePaiement=" + modePaiement + ", codePromo=" + codePromo + ", lignesCommande="
        + lignesCommande + ", facture=" + facture + ", adresseLivraison=" + adresseLivraison
        + ", adresseFacturation=" + adresseFacturation + ", paiements=" + paiements + '}';
  }
}

