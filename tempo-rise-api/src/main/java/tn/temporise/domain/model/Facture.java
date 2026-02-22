package tn.temporise.domain.model;


import lombok.Builder;

import java.time.LocalDateTime;

@Builder(toBuilder = true)

public record Facture(
        Long id,
        LocalDateTime dateEmission,
        Double total,
        Commande commande
) {}
