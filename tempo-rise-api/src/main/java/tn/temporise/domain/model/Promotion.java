package tn.temporise.domain.model;



import lombok.Builder;

import java.time.LocalDateTime;

@Builder(toBuilder = true)

public record Promotion(
        Long id,
        String nom,
        String description,
        double reduction,
        PromotionType type,
        LocalDateTime dateDebut,
        LocalDateTime dateFin,
        Produit produit
) {}
