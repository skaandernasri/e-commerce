package tn.temporise.domain.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record CommandeFilter(
        StatutCommande status,
        String email,
        Boolean orderByCreationDateDesc
) {
}
