package tn.temporise.domain.port;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.temporise.domain.model.Produit;
import tn.temporise.domain.model.ProduitFilter;

import java.util.List;

public interface ProductRepo {
  Produit save(Produit product);

  Produit findById(Long id);

  List<Produit> findAll();

  void deleteById(Long id);

  void deleteAll();

  Page<Produit> findAll(Pageable pageable);

  List<Produit> findByIds(List<Long> ids, boolean actif);

  Page<Produit> findAll(ProduitFilter filter, Pageable pageable);

  Double getMaxPrice();

  Long countAll();

  List<Produit> filteredProducts(ProduitFilter filter);

  Long countProductsWithTotalQuantityGreaterThan(Long stock);

  Long countProductsWithTotalQuantityLessThanEqual(Long stock);

  Long countProductsWithTotalQuantityEquals(Long stock);



}
