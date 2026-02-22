package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.mapstruct.factory.Mappers;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.persistence.entity.*;

import java.util.*;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring",
    uses = {DateMapper.class, PaiementMapper.class, UserMapper.class, AdresseMapper.class})
public interface CommandeMapper {
  CommandeMapper INSTANCE = Mappers.getMapper(CommandeMapper.class);


  @Mapping(target = "codePromo.code", source = "codePromo")
  @Mapping(target = "user.id", source = "utilisateurId")
  @Mapping(target = "adresseLivraison.id", source = "adresseLivraisonId")
  @Mapping(target = "adresseFacturation.id", source = "adresseFacturationId")
  Commande dtoToModel(CommandeRequest commandeRequest);

  @Mapping(target = "codePromo.code", source = "codePromo")
  @Mapping(target = "user.id", source = "utilisateurId")
  @Mapping(target = "adresseLivraison.id", source = "adresseLivraisonId")
  @Mapping(target = "adresseFacturation.id", source = "adresseFacturationId")
  Commande dtoToModel(CommandeAdminRequest commandeAdminRequest);

  @Mapping(target = "codePromo.code", source = "codePromo")
  @Mapping(target = "user.id", source = "utilisateurId")
  @Mapping(target = "adresseLivraison.id", source = "adresseLivraisonId")
  @Mapping(target = "adresseFacturation.id", source = "adresseFacturationId")
  Commande dtoToModel(CommandeGuestRequest commandeGuestRequest);

  @Mapping(target = "lignesCommande", source = "lignesCommande") // ,qualifiedByName =
  // "mapLigneCommandeEntitiesToLigneCommande"
  @Mapping(target = "adresseLivraison", source = "adresseLivraison") // ,qualifiedByName =
  // "mapAdresseEntityToAdresse"
  @Mapping(target = "adresseFacturation", source = "adresseFacturation")
  // ,qualifiedByName =
  // "mapAdresseEntityToAdresse"
  Commande entityToModel(CommandeEntity commande);

  @Mapping(target = "codePromo", source = "codePromo")
  // ,qualifiedByName = "mapCodePromo"
  CommandeEntity modelToEntity(Commande commande);


  @Mapping(target = "utilisateur", source = "user")
  @Mapping(target = "produits", source = "lignesCommande") // ,qualifiedByName =
  @Mapping(target = "utilisateur.nom", source = "nom")
  @Mapping(target = "utilisateur.prenom", source = "prenom")
  @Mapping(target = "utilisateur.telephone", source = "telephone")
  CommandeResponse modelToDto(Commande commande);

  default CodePromoEntity map(CodePromo codePromo) {
    if (codePromo == null || codePromo.id() == null)
      return null;
    CodePromoEntity codePromoEntity = new CodePromoEntity();
    codePromoEntity.setCode(codePromo.code());
    codePromoEntity.setId(codePromo.id());
    codePromoEntity.setReduction(codePromo.reduction());
    codePromoEntity.setDateExpiration(codePromo.dateExpiration());
    return codePromoEntity;
  }

  default List<LigneCommande> mapLigneCommandeEntitiesToLigneCommande(
      List<LigneCommandeEntity> ligneCommandeEntities) {
    if (ligneCommandeEntities == null || ligneCommandeEntities.isEmpty()) {
      return Collections.emptyList();
    }
    return ligneCommandeEntities.stream()
        .map(lc -> LigneCommande.builder().id(lc.getId()).quantite(lc.getQuantite())
            .prixTotal(lc.getPrixTotal()).variant(mapVariantEntityToVariant(lc.getVariant()))
            .commande(Commande.builder().id(lc.getCommande().getId()).build()).build())
        .toList();
  }

  default List<CommandeResponseProduitsInner> mapLigneCommandeToProduct(
      List<LigneCommande> ligneCommandes) {
    if (ligneCommandes == null || ligneCommandes.isEmpty()) {
      return Collections.emptyList();
    }
    return ligneCommandes.stream().map(lc -> {
      CommandeResponseProduitsInner commandeResponseProduitsInner =
          new CommandeResponseProduitsInner();
      commandeResponseProduitsInner.setVariantId(lc.variant().id());
      commandeResponseProduitsInner.setProductId(lc.variant().produit().id());
      commandeResponseProduitsInner.setNom(lc.variant().produit().nom());
      commandeResponseProduitsInner.setPrix(lc.prixTotal() / lc.quantite());
      commandeResponseProduitsInner.setCategorie(lc.variant().produit().categorie().nom());
      commandeResponseProduitsInner.setQuantite(lc.quantite());
      commandeResponseProduitsInner.setCouleur(lc.variant().color());
      commandeResponseProduitsInner.setTaille(lc.variant().size());
      commandeResponseProduitsInner
          .setImageProduits(mapImageToImagesResponse(lc.variant().produit().imageProduits()));
      commandeResponseProduitsInner.setPrixTotal(lc.prixTotal());
      return commandeResponseProduitsInner;
    }

    ).toList();
  }

  default List<CommandeResponseProduitsInnerImageProduitsInner> mapImageToImagesResponse(
      Set<ImageProduit> imageProduits) {
    if (imageProduits == null || imageProduits.isEmpty()) {
      return Collections.emptyList();
    }
    return imageProduits.stream().map(imageProduit -> {
      CommandeResponseProduitsInnerImageProduitsInner commandeResponseProduitsInnerImageProduitsInner =
          new CommandeResponseProduitsInnerImageProduitsInner();
      commandeResponseProduitsInnerImageProduitsInner.setId(imageProduit.id());
      commandeResponseProduitsInnerImageProduitsInner.setUrl(imageProduit.url());
      return commandeResponseProduitsInnerImageProduitsInner;
    }

    ).toList();
  }

  default Produit mapProductEntityToProduct(ProduitEntity produitEntity) {
    if (produitEntity == null || produitEntity.getId() == null)
      return null;
    return Produit.builder().id(produitEntity.getId()).prix(produitEntity.getPrix())
        .nom(produitEntity.getNom()).description(produitEntity.getDescription())
        .promotions(mapPromotionEntitiesToPromotions(produitEntity.getPromotions()))
        .imageProduits(mapImageEntitiesToImages(produitEntity.getImageProduits()))
        .categorie(mapCategorieEntityToCategorie(produitEntity.getCategorie())).build();
  }

  default Categorie mapCategorieEntityToCategorie(CategorieEntity categorieEntity) {
    if (categorieEntity == null || categorieEntity.getId() == null)
      return null;
    return Categorie.builder().id(categorieEntity.getId()).nom(categorieEntity.getNom())
        .description(categorieEntity.getDescription()).build();
  }

  default Variant mapVariantEntityToVariant(VariantEntity variantEntity) {
    if (variantEntity == null || variantEntity.getId() == null)
      return null;
    return Variant.builder().id(variantEntity.getId()).color(variantEntity.getColor())
        .size(variantEntity.getSize())
        .produit(mapProductEntityToProduct(variantEntity.getProduit()))
        .quantity(variantEntity.getQuantity()).build();
  }

  default Set<Promotion> mapPromotionEntitiesToPromotions(Set<PromotionEntity> promotionEntities) {
    if (promotionEntities == null || promotionEntities.isEmpty()) {
      return Collections.emptySet();
    }
    return promotionEntities.stream()
        .map(promotionEntity -> Promotion.builder().id(promotionEntity.getId())
            .nom(promotionEntity.getNom()).description(promotionEntity.getDescription())
            .reduction(promotionEntity.getReduction()).dateDebut(promotionEntity.getDateDebut())
            .dateFin(promotionEntity.getDateFin()).type(promotionEntity.getType()).build())
        .collect(Collectors.toSet());
  }

  default Set<ImageProduit> mapImageEntitiesToImages(Set<ImageProduitEntity> imageProduitEntities) {
    if (imageProduitEntities == null || imageProduitEntities.isEmpty()) {
      return Collections.emptySet();
    }
    return imageProduitEntities.stream().map(imageProduitEntity -> ImageProduit.builder()
        .id(imageProduitEntity.getId()).url(imageProduitEntity.getUrl()).build())
        .collect(Collectors.toSet());
  }

  default Facture mapFactureEntityToFacture(FactureEntity factureEntity) {
    if (factureEntity == null || factureEntity.getId() == null)
      return null;
    return Facture.builder().id(factureEntity.getId())
        .dateEmission((factureEntity.getDateEmission())).total(factureEntity.getTotal())
        .commande(Commande.builder().id(factureEntity.getCommande().getId()).build()).build();
  }

  default UtilisateurModel mapUtilisateurEntityToUtilisateurModel(
      UtilisateurEntity utilisateurEntity) {
    if (utilisateurEntity == null || utilisateurEntity.getId() == null)
      return null;
    return UtilisateurModel.builder().email(utilisateurEntity.getEmail())
        .id(utilisateurEntity.getId()).nom(utilisateurEntity.getNom())
        .prenom(utilisateurEntity.getPrenom()).telephone(utilisateurEntity.getTelephone()).build();
  }

  default CommandeEntity mapIdToCommandeEntity(Long id) {
    if (id == null)
      return null;
    CommandeEntity commande = new CommandeEntity();
    commande.setId(id);
    return commande;
  }
}
