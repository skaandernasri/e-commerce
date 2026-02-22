package tn.temporise.domain.model;



import lombok.Builder;

import java.time.LocalDateTime;

@Builder

public record Avis (  Long id,
        int note,
        String commentaire,
        LocalDateTime datePublication,


        UtilisateurModel utilisateur,


        Produit produit){ }
