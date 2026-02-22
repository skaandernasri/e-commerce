package tn.temporise.domain.model;



import lombok.Builder;

import java.util.Set;

@Builder(toBuilder = true)

public record Panier(
        Long id,
        Set<Variant> variants,
        UtilisateurModel utilisateur
) {
}
