package tn.temporise.domain.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record Variant(
    Long id,
    Produit produit,
    String color,
    String size,
    Long quantity,
    Long reservedQuantity,
    Long availableQuantity
) {
    @Override
    public String toString() {
        return "ProductVariant{" +
                "id=" + id +
                ", produit=" + produit +
                ", color='" + color + '\'' +
                ", size='" + size + '\'' +
                ", quantity=" + quantity +
                ", availableQuantity=" + availableQuantity +
                '}';
    }
}
