package tn.temporise.domain.port;

import tn.temporise.domain.model.Variant;

import java.util.List;


public interface VariantRepo {
  Variant save(Variant variant);

  Variant findById(Long id);

  List<Variant> findAll();

  Variant findByIdForUpdate(Long id);

  boolean existsByColorAndSizeAndProduitId(String color, String size, Long productId);

  void saveAll(List<Variant> variants);

  void deleteById(Long id);

  Long countByQuantityreaterThan(Long quantity);

  Long countByQuantityLessThan(Long quantity);

  Long countByQuantityEquals(Long quantity);

}
