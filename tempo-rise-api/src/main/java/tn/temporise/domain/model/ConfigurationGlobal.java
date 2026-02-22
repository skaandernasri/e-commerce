package tn.temporise.domain.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record ConfigurationGlobal(
        long id,
        Float valeurLivraison,
        Float seuilLivraisonGratuite
) {
}
