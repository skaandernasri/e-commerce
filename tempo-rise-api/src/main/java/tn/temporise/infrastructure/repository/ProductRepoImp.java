package tn.temporise.infrastructure.repository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import tn.temporise.application.mapper.ProductMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.Produit;
import tn.temporise.domain.model.ProduitFilter;
import tn.temporise.domain.port.ProductRepo;
import tn.temporise.infrastructure.persistence.entity.AvisEntity;
import tn.temporise.infrastructure.persistence.entity.ProduitEntity;
import tn.temporise.infrastructure.persistence.entity.PromotionEntity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
@Slf4j
public class ProductRepoImp implements ProductRepo {
  private final ProductJpaRepo productJpaRepo;
  private final ProductMapper productMapper;
  private final ExceptionFactory exceptionFactory;

  @Override
  public Produit save(Produit product) {
    ProduitEntity produitEntity = productJpaRepo.save(productMapper.modelToEntity(product));
    log.info("Product saved: " + produitEntity);
    return productMapper.entityToModel(produitEntity);
  }

  @Override
  public Produit findById(Long id) {
    log.info("Finding product by ID: " + id);
    return productJpaRepo.findById(id).map(productMapper::entityToModel)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.product")); // Retourne null si le
                                                                           // produit n'existe pas
  }

  @Override
  public List<Produit> findAll() {
    if (productJpaRepo.findAll().isEmpty())
      throw exceptionFactory.notFound("notfound.no_products");
    return productJpaRepo.findAll().stream().map(productMapper::entityToModel)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteById(Long id) {
    productJpaRepo.deleteById(id);
  }

  @Override
  public void deleteAll() {
    productJpaRepo.deleteAll();
  }

  @Override
  public Page<Produit> findAll(Pageable pageable) {
    return productJpaRepo.findAll(pageable).map(productMapper::entityToModel);
  }

  @Override
  public List<Produit> findByIds(List<Long> ids, boolean actif) {
    List<ProduitEntity> produitEntities = productJpaRepo.findAllByIdInAndActif(ids, actif); // Récupère
                                                                                            // les
    // produits par leurs
    // IDs>
    return produitEntities.stream().map(productMapper::entityToModel).toList();
  }

  @Override
  public Page<Produit> findAll(ProduitFilter filter, Pageable pageable) {
    Specification<ProduitEntity> spec = (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      // --- Filtering ---
      if (filter.productName() != null) {
        predicates.add(
            cb.like(cb.lower(root.get("nom")), "%" + filter.productName().toLowerCase() + "%"));
      }

      if (filter.categoryNames() != null && !filter.categoryNames().isEmpty()) {
        predicates.add(root.get("categorie").get("nom").in(
            // filter.categoryNames().stream().map(String::toLowerCase).collect(Collectors.toList())));
            new ArrayList<>(filter.categoryNames())));
      }

      if (filter.minPrice() != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("prix"), filter.minPrice()));
      }

      if (filter.maxPrice() != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("prix"), filter.maxPrice()));
      }

      if ((filter.minPromotion() != null) || filter.maxPromotion() != null) {
        Join<ProduitEntity, PromotionEntity> join = root.join("promotions", JoinType.LEFT);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        Predicate activePromotion = cb.and(cb.lessThanOrEqualTo(join.get("dateDebut"), now),
            cb.greaterThanOrEqualTo(join.get("dateFin"), now));

        if (filter.minPromotion() != null) {
          activePromotion = cb.and(activePromotion,
              cb.greaterThanOrEqualTo(join.get("reduction"), filter.minPromotion()));
        }
        if (filter.maxPromotion() != null) {
          activePromotion = cb.and(activePromotion,
              cb.lessThanOrEqualTo(join.get("reduction"), filter.maxPromotion()));
        }
        if (filter.minPromotion() != null && filter.minPromotion() == 0) {
          Predicate hasNoPromotion = cb.isNull(join.get("id"));
          Predicate inactivePromotion = cb.or(cb.greaterThan(join.get("dateDebut"), now),
              cb.lessThan(join.get("dateFin"), now));
          activePromotion = cb.or(activePromotion, hasNoPromotion, inactivePromotion);
        }

        predicates.add(activePromotion);
      }

      if (filter.actif() != null) {
        predicates.add(cb.equal(root.get("actif"), filter.actif()));
      }

      // --- Sorting by average review ---
      if (filter.orderByRatingDesc() != null) {
        if (filter.orderByRatingDesc()) {
          Join<ProduitEntity, AvisEntity> joinAvis = root.join("avis", JoinType.LEFT);
          query.groupBy(root.get("id"));
          query.orderBy(cb.desc(cb.avg(joinAvis.get("note"))));
        } else {
          Join<ProduitEntity, AvisEntity> joinAvis = root.join("avis", JoinType.LEFT);
          query.groupBy(root.get("id"));
          query.orderBy(cb.asc(cb.avg(joinAvis.get("note"))));
        }
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
    return productJpaRepo.findAll(spec, pageable).map(productMapper::entityToModel);
  }

  @Override
  public Double getMaxPrice() {
    if (productJpaRepo.getMaxPrice() == null)
      return 0.0;
    return productJpaRepo.getMaxPrice();
  }


  @Override
  public Long countAll() {
    return productJpaRepo.count();
  }

  @Override
  public List<Produit> filteredProducts(ProduitFilter filter) {
    Specification<ProduitEntity> spec = Specification.where(null);
    if (filter.productName() != null
        && (filter.categoryNames() != null && !filter.categoryNames().isEmpty())) {
      spec = spec.and((root, query, cb) -> cb.or(
          cb.like(cb.lower(root.get("nom")), "%" + filter.productName().toLowerCase() + "%"),
          cb.like(cb.lower(root.get("categorie").get("nom")),
              "%" + filter.categoryNames().getFirst().toLowerCase() + "%")));
    }
    if (filter.actif() != null) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("actif"), filter.actif()));
    }
    log.info("Filtering products: {}", filter);
    return productJpaRepo.findAll(spec).stream().map(productMapper::entityToModel).toList();
  }

  @Override
  public Long countProductsWithTotalQuantityGreaterThan(Long stock) {
    return productJpaRepo.countProductsWithTotalQuantityGreaterThan(stock);
  }

  @Override
  public Long countProductsWithTotalQuantityLessThanEqual(Long stock) {
    return productJpaRepo.countProductsWithTotalQuantityLessThanEqual(stock);
  }

  @Override
  public Long countProductsWithTotalQuantityEquals(Long stock) {
    return productJpaRepo.countProductsWithTotalQuantityEquals(stock);
  }

}
