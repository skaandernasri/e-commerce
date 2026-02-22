package tn.temporise.domain.model;

import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record ProduitFilter(
        String productName, List<String> categoryNames, Double minPrice, Double maxPrice, Double minPromotion, Double maxPromotion,
        Boolean actif,Boolean orderByRatingDesc){}
