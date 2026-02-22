package tn.temporise.domain.port;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.temporise.domain.model.Commande;
import tn.temporise.domain.model.CommandeFilter;
import tn.temporise.domain.model.StatutCommande;
import tn.temporise.domain.model.UserType;

import java.time.LocalDateTime;
import java.util.List;

public interface CommandeRepo {
  Commande save(Commande commande);

  Commande findById(Long id);

  List<Commande> findAll();

  Page<Commande> findAll(CommandeFilter filter, Pageable pageable);

  Double findTotalCommandes();

  Double findTotalCommandesByStatut(StatutCommande statut);

  Long countCommandes();

  void deleteById(Long id);

  void deleteAll();

  List<Commande> findByUserId(Long id);

  List<Commande> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

  Commande findByCodePromoId(Long id);

  boolean existsByIdAndUserId(Long id, Long userId);

  boolean existsByIdAndUser_IdAndUserType(Long id, Long userId, UserType userType);

  List<Commande> findByAdresseFacturationId(Long id);

  List<Commande> findByAdresseLivraisonId(Long id);

}
