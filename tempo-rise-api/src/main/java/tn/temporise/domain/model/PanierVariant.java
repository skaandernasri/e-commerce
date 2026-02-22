package tn.temporise.domain.model;

import lombok.Builder;
import tn.temporise.infrastructure.persistence.entity.PanierVariantId;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record PanierVariant(
    PanierVariantId id,
    Variant variant,
    Panier panier,
    Long quantite,
    LocalDateTime expirationDate
) {
    @Override
    public String toString() {
        return "PanierProduit{" +
                "id=" + id +
                ", variant=" + variant +
                ", panier=" + panier +
                ", quantite=" + quantite +
                ", expirationDate=" + expirationDate +
                '}';
    }
}
