package tn.temporise.domain.model;

import lombok.Builder;

import java.time.LocalDateTime;
@Builder(toBuilder = true)
public record WhishlistItem(
        Long id,
        Produit produit,
        Whishlist whishlist,
        LocalDateTime addedAt
) {
}
