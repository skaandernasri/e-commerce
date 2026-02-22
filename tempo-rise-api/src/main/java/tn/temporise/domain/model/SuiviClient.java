package tn.temporise.domain.model;



import lombok.Builder;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record SuiviClient(
        Long id,
        UtilisateurModel utilisateur,
        Produit produit,
        TypeAction typeAction,
        double score,
        LocalDateTime date,
        UtilisateurAnonyme utilisateurAnonyme
) {
    @Override
    public String toString() {
        return "SuiviClient{" +
                "id=" + id +
                ", utilisateur=" + utilisateur +
                ", produit=" + produit +
                ", typeAction=" + typeAction +
                ", score=" + score +
                ", date=" + date +
                ", utilisateurAnonyme=" + utilisateurAnonyme +
                '}';
    }
}
