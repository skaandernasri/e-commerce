package tn.temporise.domain.port;

import tn.temporise.domain.model.Whishlist;

public interface WhishlistRepo {
  Whishlist save(Whishlist whishlist);

  Whishlist findById(Long id);

  Whishlist findByUserId(Long userId);
}
