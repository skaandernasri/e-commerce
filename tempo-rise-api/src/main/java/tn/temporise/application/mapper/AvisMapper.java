package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.persistence.entity.AvisEntity;
import tn.temporise.infrastructure.persistence.entity.ProduitEntity;



@Mapper(componentModel = "spring", uses = {DateMapper.class})
public interface AvisMapper {
  @Mapping(target = "utilisateur", source = "utilisateurId",
      qualifiedByName = "mapUtilisateurIdToUtilisateur")
  @Mapping(target = "produit", source = "produitId", qualifiedByName = "mapProduitIdToProduit")
  Avis dtoToModel(AvisRequest avisRequest);

  AvisEntity modelToEntity(Avis avis);

  @Mapping(target = "produit", source = "produit", qualifiedByName = "mapProduitEntitiesToProduits")
  Avis entityToModel(AvisEntity avisEntity);

  AvisResponse modelToResponse(Avis avis);

  AvisResponse entityToResponse(AvisEntity avisEntity);

  @Named("mapUtilisateurIdToUtilisateur")
  default UtilisateurModel mapUtilisateurIdToUtilisateur(Long id) {
    if (id == null)
      return null;
    return UtilisateurModel.builder().id(id).build();
  }

  @Named("mapProduitIdToProduit")
  default Produit mapProduitIdToProduit(Long id) {
    if (id == null)
      return null;
    return Produit.builder().id(id).build();
  }

  @Named("mapProduitEntitiesToProduits")
  default Produit mapProduitEntitiesToProduits(ProduitEntity produit) {
    if (produit == null) {
      return null;
    }
    return Produit.builder().id(produit.getId()).nom(produit.getNom())
        .description(produit.getDescription()).build();
  }

}
