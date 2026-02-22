package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.persistence.entity.PanierEntity;


import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring",
    uses = {ProductMapper.class, VariantMapper.class, DateMapper.class})
public interface PanierMapper {

  @Mapping(target = "utilisateur.id", source = "utilisateurId")
  @Mapping(target = "variants", source = "articles")
  Panier dtoToModel(PanierRequest panierRequest);

  @Mapping(target = "utilisateurId", source = "utilisateur.id")
  @Mapping(target = "articles", source = "variants", qualifiedByName = "mapVariantsToArticles")
  PanierResponse modelToResponse(Panier panier);

  @Mapping(target = "utilisateur.id", source = "utilisateurId")
  @Mapping(target = "variants", source = "articles")
  Panier responseToModel(PanierResponse panierResponse);

  @Mapping(target = "utilisateurId", source = "utilisateur.id")
  PanierResponse entityToResponse(PanierEntity panier);

  Panier entityToModel(PanierEntity panier);

  @Mapping(target = "panierProduit", ignore = true)
  PanierEntity modelToEntity(Panier panier);

  @Named("mapVariantsToArticles")
  default List<PanierResponseArticlesInner> mapVariantsToArticles(Set<Variant> variants) {
    if (variants == null)
      return List.of();

    return variants.stream().collect(Collectors.groupingBy(Variant::produit)).entrySet().stream()
        .map(entry -> {
          Produit produit = entry.getKey();
          List<Variant> produitVariants = entry.getValue();
          PanierResponseArticlesInner panierResponseArticlesInner =
              new PanierResponseArticlesInner();
          panierResponseArticlesInner.setId(produit.id());
          panierResponseArticlesInner.setNom(produit.nom());
          panierResponseArticlesInner.setDescription(produit.description());
          panierResponseArticlesInner.setPrix(produit.prix());
          panierResponseArticlesInner.setCategorie(mapCategorie(produit.categorie()));
          panierResponseArticlesInner
              .setPromotions(mapPromotions(produit.promotions().stream().toList()));
          panierResponseArticlesInner
              .setImageProduits(mapImageProduits(produit.imageProduits().stream().toList()));
          panierResponseArticlesInner
              .setVariants(produitVariants.stream().map(this::mapVariant).toList());
          return panierResponseArticlesInner;
        }).toList();
  }

  VariantResponse mapVariant(Variant variant);

  default PanierResponseArticlesInnerCategorie mapCategorie(Categorie categorie) {
    PanierResponseArticlesInnerCategorie categorieResponse =
        new PanierResponseArticlesInnerCategorie();
    categorieResponse.setId(categorie.id());
    categorieResponse.setNom(categorie.nom());
    categorieResponse.setDescription(categorie.description());
    return categorieResponse;
  }

  default List<PanierResponseArticlesInnerPromotionsInner> mapPromotions(
      List<Promotion> promotions) {
    if (promotions == null)
      return List.of();
    return promotions.stream().map(p -> {
      PanierResponseArticlesInnerPromotionsInner promotionResponse =
          new PanierResponseArticlesInnerPromotionsInner();
      promotionResponse.setId(p.id());
      promotionResponse.setNom(p.nom());
      promotionResponse.setReduction(p.reduction());
      promotionResponse.setType(p.type().name());
      promotionResponse.setDateDebut(DateMapper.INSTANCE.mapDateToOffsetDateTime(p.dateDebut()));
      promotionResponse.setDateFin(DateMapper.INSTANCE.mapDateToOffsetDateTime(p.dateFin()));
      return promotionResponse;
    }).toList();
  }

  default List<PanierResponseArticlesInnerImageProduitsInner> mapImageProduits(
      List<ImageProduit> images) {
    if (images == null)
      return List.of();
    return images.stream().map(i -> {
      PanierResponseArticlesInnerImageProduitsInner imageProduitResponse =
          new PanierResponseArticlesInnerImageProduitsInner();
      imageProduitResponse.setId(i.id());
      imageProduitResponse.setUrl(i.url());
      return imageProduitResponse;
    }).toList();
  }

}
