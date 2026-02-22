package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.web.multipart.MultipartFile;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.persistence.entity.ImageProduitEntity;
import tn.temporise.infrastructure.persistence.entity.ProduitEntity;



@Mapper(componentModel = "spring")
public interface ImageProductMapper {
  @Mapping(target = "produit", source = "produitId", qualifiedByName = "mapProduitIdToProduit")
  ImageProduit dtoToModel(Long produitId, MultipartFile contenu);

  ImageProduitEntity modelToEntity(ImageProduit imageProduit);

  @Mapping(target = "produit", source = "produit", qualifiedByName = "mapProduitEntitiesToProduits")
  ImageProduit entityToModel(ImageProduitEntity imageProduit);

  ImageProduitResponse modelToResponse(ImageProduit imageProduit);

  ImageProduitResponse entityToResponse(ImageProduitEntity imageProduit);

  @Named("mapProduitIdToProduit")
  default Produit mapProduitIdToProduit(Long produitId) {
    if (produitId == null) {
      return null;
    }
    return Produit.builder().id(produitId).build();
  }

  @Named("mapProduitEntitiesToProduits")
  default Produit mapProduitEntitiesToProduits(ProduitEntity produit) {
    if (produit == null) {
      return null;
    }
    return Produit.builder().id(produit.getId()).nom(produit.getNom())
        .description(produit.getDescription()).build();
  }

  default byte[] toByteArray(MultipartFile file) throws Exception {
    if (file == null)
      return null;
    return file.getBytes();
  }
}
