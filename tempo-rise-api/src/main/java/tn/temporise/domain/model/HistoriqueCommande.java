package tn.temporise.domain.model;


import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

@Builder

public record HistoriqueCommande(
        Long id,
        LocalDateTime dateCommande,
        StatutCommande statut,
        Set<Produit> produits,
        UtilisateurModel user
) {}
