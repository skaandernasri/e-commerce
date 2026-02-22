package tn.temporise.domain.model;



import lombok.Builder;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record RetourProduit(
        Long id,
        String raisonRetour,
        LocalDateTime dateRetour,
        Variant variant,
        UtilisateurModel utilisateur
) {}