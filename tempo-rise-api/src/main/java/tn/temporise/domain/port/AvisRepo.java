package tn.temporise.domain.port;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.temporise.domain.model.Avis;
import tn.temporise.domain.model.AvisFilter;

import java.time.LocalDateTime;
import java.util.List;

public interface AvisRepo {
  Avis save(Avis avis);

  Avis findById(Long id);

  List<Avis> findAll();

  Avis update(Avis avis);

  void deleteById(Long id);

  void deleteAll();

  List<Avis> findByProduitId(Long id);

  List<Avis> findByUtilisateurId(Long id);

  Page<Avis> findAllByProduitId(Long id, Pageable pageable);

  Page<Avis> findAll(AvisFilter filter, Pageable pageable);

  Float findAverageRating();

  Float findAverageRatingByProduitName(String productName);

  Long findReviewsInPeriod(LocalDateTime startDate, LocalDateTime endDate);

  Long findReviewsInPeriodByProduitName(LocalDateTime startDate, LocalDateTime endDate,
      String productName);

  Float findAverageRatingInPeriod(LocalDateTime startDate, LocalDateTime endDate);

  Float findAverageRatingInPeriodByProduitName(LocalDateTime startDate, LocalDateTime endDate,
      String productName);

}
