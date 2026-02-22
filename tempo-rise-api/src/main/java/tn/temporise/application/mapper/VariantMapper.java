package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.temporise.domain.model.Variant;
import tn.temporise.domain.model.VariantRequest;
import tn.temporise.domain.model.VariantResponse;
import tn.temporise.infrastructure.persistence.entity.VariantEntity;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface VariantMapper {
  Variant ToModel(VariantRequest request);

  List<VariantEntity> modelToEntities(List<Variant> variants);

  VariantEntity modelToEntity(Variant variant);

  Variant entityToModel(VariantEntity entity);

  @Mapping(target = "productId", source = "produit.id")
  VariantResponse modelToResponse(Variant variant);

  default List<Variant> dtoToModel(List<VariantRequest> request) {
    if (request == null || request.isEmpty())
      return List.of();
    return request.stream().map(this::ToModel).collect(Collectors.toList());
  }
}
