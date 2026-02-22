package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.persistence.entity.*;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {DateMapper.class})
public interface ProductMapper {

  // Map ProductRequest to Produit
  @Mapping(target = "categorie", source = "categorie",
      qualifiedByName = "mapCategorieIdToCategorie")
  @Mapping(target = "promotions", source = "promotions",
      qualifiedByName = "mapPromotionIdToPromotion")
  @Mapping(target = "variants", ignore = true)
  Produit dtoToModel(ProductRequest productRequest);

  // Map Produit to ProduitEntity
  @Mapping(target = "categorie", source = "categorie")
  ProduitEntity modelToEntity(Produit produit);

  // Map ProduitEntity to Produit
  @Mapping(target = "promotions", source = "promotions",
      qualifiedByName = "mapPromotionEntitiesToPromotions")
  @Mapping(target = "imageProduits", source = "imageProduits",
      qualifiedByName = "mapImageEntitiesToImages")
  @Mapping(target = "categorie", source = "categorie")
  @Mapping(target = "avis", source = "avis", qualifiedByName = "mapAvisEntitiesToAvis")
  @Mapping(target = "variants", source = "variants",
      qualifiedByName = "mapVariantEntitiesToVariants")
  Produit entityToModel(ProduitEntity produitEntity);

  // Map Produit to ProductResponse
  @Mapping(target = "categorie", source = "categorie")
  @Mapping(target = "promotions", source = "promotions")
  ProductResponse modelToResponse(Produit produit);

  // Map ProduitEntity to ProductResponse
  @Mapping(target = "categorie", source = "categorie")
  ProductResponse entityToResponse(ProduitEntity produit);

  // Map Long to Categorie
  @Named("mapCategorieIdToCategorie")
  default Categorie mapCategorieIdToCategorie(Long categorieId) {
    if (categorieId == null) {
      return null;
    }
    return new Categorie(categorieId);
  }

  @Named("mapPromotionIdToPromotion")
  default Set<Promotion> mapPromotionIdToPromotion(List<Long> promotionsId) {
    if (promotionsId.isEmpty()) {
      return null;
    }
    return promotionsId.stream().map(id -> Promotion.builder().id(id).build())
        .collect(Collectors.toSet());
  }


  @Named("mapPromotionEntitiesToPromotions")
  default Set<Promotion> mapPromotionEntitiesToPromotions(Set<PromotionEntity> promotionEntities) {
    if (promotionEntities == null || promotionEntities.isEmpty()) {
      return Collections.emptySet();
    }
    return promotionEntities.stream()
        .map(promotionEntity -> Promotion.builder().id(promotionEntity.getId())
            .nom(promotionEntity.getNom()).description(promotionEntity.getDescription())
            .reduction(promotionEntity.getReduction()).type(promotionEntity.getType())
            .dateDebut(promotionEntity.getDateDebut()).dateFin(promotionEntity.getDateFin())
            .build())
        .collect(Collectors.toSet());
  }

  default List<PanierResponseArticlesInnerPromotionsInner> mapPromotionToPromotionResponse(
      List<Promotion> promotions) {
    if (promotions.isEmpty()) {
      return null;
    }
    PanierResponseArticlesInnerPromotionsInner panierResponseArticlesInnerPromotionsInner =
        new PanierResponseArticlesInnerPromotionsInner();
    return promotions.stream().map(promotion -> {
      panierResponseArticlesInnerPromotionsInner.setNom(promotion.nom());
      panierResponseArticlesInnerPromotionsInner.setReduction(promotion.reduction());
      panierResponseArticlesInnerPromotionsInner
          .setDateDebut(DateMapper.INSTANCE.mapDateToOffsetDateTime(promotion.dateDebut()));
      panierResponseArticlesInnerPromotionsInner
          .setDateFin(DateMapper.INSTANCE.mapDateToOffsetDateTime(promotion.dateFin()));
      panierResponseArticlesInnerPromotionsInner.setType(promotion.type().name());
      return panierResponseArticlesInnerPromotionsInner;
    }).toList();
  }

  @Named("mapVariantEntitiesToVariants")
  default Set<Variant> mapVariantEntitiesToVariants(Set<VariantEntity> variantEntities) {
    if (variantEntities == null || variantEntities.isEmpty()) {
      return Collections.emptySet();
    }
    return variantEntities.stream()
        .map(variantEntity -> Variant.builder().id(variantEntity.getId())
            .color(variantEntity.getColor()).size(variantEntity.getSize())
            .quantity(variantEntity.getQuantity()).build())
        .collect(Collectors.toSet());
  }

  @Named("mapImageEntitiesToImages")
  default Set<ImageProduit> mapImageEntitiesToImages(Set<ImageProduitEntity> imageProduitEntities) {
    if (imageProduitEntities == null || imageProduitEntities.isEmpty()) {
      return Collections.emptySet();
    }
    return imageProduitEntities.stream().map(imageProduitEntity -> ImageProduit.builder()
        .id(imageProduitEntity.getId()).url(imageProduitEntity.getUrl()).build())
        .collect(Collectors.toSet());
  }

  @Named("mapAvisEntitiesToAvis")
  default Set<Avis> mapAvisEntitiesToAvis(Set<AvisEntity> avisEntities) {
    if (avisEntities == null || avisEntities.isEmpty()) {
      return Collections.emptySet();
    }
    return avisEntities.stream()
        .map(avisEntity -> Avis.builder().id(avisEntity.getId()).note(avisEntity.getNote())
            .commentaire(avisEntity.getCommentaire())
            .datePublication(avisEntity.getDatePublication())
            .utilisateur(UtilisateurModel.builder().id(avisEntity.getUtilisateur().getId())
                .nom(avisEntity.getUtilisateur().getNom())
                .prenom(avisEntity.getUtilisateur().getPrenom())
                .email(avisEntity.getUtilisateur().getEmail()).build())
            .build())
        .collect(Collectors.toSet());
  }

}

