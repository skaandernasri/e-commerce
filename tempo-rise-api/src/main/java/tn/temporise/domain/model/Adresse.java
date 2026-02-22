package tn.temporise.domain.model;



import lombok.Builder;

import java.util.Set;
@Builder(toBuilder = true)
public record Adresse(
         Long id,
         String ligne1,
         String ligne2,
         String ville,
         String pays,
         String codePostal,
         TypeAdresse type,
         UtilisateurModel utilisateur,
         Set<Commande> commandesLivraison,
         Set<Commande> commandesFacturation
) {
}
