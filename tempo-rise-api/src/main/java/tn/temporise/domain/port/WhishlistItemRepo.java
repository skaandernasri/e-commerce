package tn.temporise.domain.port;

import tn.temporise.domain.model.WhishlistItem;

import java.util.List;

public interface WhishlistItemRepo {
  WhishlistItem save(WhishlistItem whishlistItem);

  List<WhishlistItem> findByWhishlistId(Long whishlistId);

  boolean existsByWhishlistIdAndProduitId(Long whishlistId, Long produitId);

  void removeAllItems(Long userId);

  void removeItem(Long userId, Long productId);


}
