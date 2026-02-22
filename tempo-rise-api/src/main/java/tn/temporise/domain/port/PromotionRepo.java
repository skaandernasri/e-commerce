package tn.temporise.domain.port;

import tn.temporise.domain.model.Promotion;

import java.time.LocalDateTime;
import java.util.List;

public interface PromotionRepo {
  Promotion save(Promotion promotion);

  Promotion findById(Long id);

  List<Promotion> findAll();

  Promotion update(Promotion produit);

  void deleteById(Long id);

  void deleteAll();

  List<Promotion> findByProduitId(Long id);

  List<Promotion> findActivePromotions(LocalDateTime currentDate);

  Promotion findActivePromotionByProduct(LocalDateTime currentDate, Long productId);

  List<Promotion> findInActivePromotions(LocalDateTime currentDate);

  List<Promotion> saveAll(List<Promotion> promotions);
}
