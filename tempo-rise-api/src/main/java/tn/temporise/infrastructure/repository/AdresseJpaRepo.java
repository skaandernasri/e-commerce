package tn.temporise.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.temporise.domain.model.TypeAdresse;
import tn.temporise.infrastructure.persistence.entity.AdresseEntity;

import java.util.List;

@Repository
public interface AdresseJpaRepo extends JpaRepository<AdresseEntity, Long> {
  // @Query("SELECT a FROM AdresseEntity a WHERE a.utilisateur.id = :id")
  List<AdresseEntity> findByUtilisateur_Id(@Param("id") Long id);

  List<AdresseEntity> findByType(TypeAdresse type);
}
