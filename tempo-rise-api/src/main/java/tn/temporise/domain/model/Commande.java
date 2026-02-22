package tn.temporise.domain.model;


import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Builder(toBuilder = true)
public record Commande(
        Long id,
        LocalDateTime date,
        String nom,
        String prenom,
        String email,
        String telephone,
        StatutCommande statut,
        UtilisateurModel user,
        ModePaiement modePaiement,
        CodePromo codePromo,
        double total,
        List<LigneCommande> lignesCommande,
        Facture facture,
        Adresse adresseLivraison,
        Adresse adresseFacturation,
        Set<Paiement> paiements
) {
    @Override
    public String toString() {
        return "Commande{" +
                "id=" + id +
                ", date=" + date +
                ", statut=" + statut +
                ", user=" + user +
                ", modePaiement=" + modePaiement +
                ", codePromo=" + codePromo +
                ", total=" + total +
                ", lignesCommande=" + lignesCommande +
                ", facture=" + facture +
                ", adresseLivraison=" + adresseLivraison +
                ", adresseFacturation=" + adresseFacturation +
                ", paiement=" + paiements +
                '}';
    }
}
