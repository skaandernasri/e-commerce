package tn.temporise.domain.model;


import lombok.Builder;

@Builder(toBuilder = true)

public record LigneCommande(
        Long id,
        Variant variant,
        long quantite,
        double prixTotal,
        Commande commande
) {
    @Override
    public String toString() {
        return "LigneCommande{" +
                "id=" + id +
                ", variant=" + variant +
                ", quantite=" + quantite +
                ", prixTotal=" + prixTotal +
                ", commande=" + commande +
                '}';
    }
}
