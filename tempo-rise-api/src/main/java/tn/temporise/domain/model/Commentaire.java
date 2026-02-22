package tn.temporise.domain.model;


import lombok.Builder;

import java.util.Date;

@Builder

public record Commentaire(
        Long id,
        String contenu,
        Date datePublication,
        UtilisateurModel user,
        BlogPost blogPost
) {}
