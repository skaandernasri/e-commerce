package tn.temporise.domain.model;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

@Builder(toBuilder = true)
public record Whishlist (
     Long id,
     UtilisateurModel utilisateur,
     LocalDateTime createdAt,
     Set<Produit> produits
){}

