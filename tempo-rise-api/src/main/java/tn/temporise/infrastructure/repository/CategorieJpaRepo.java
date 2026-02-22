package tn.temporise.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.temporise.infrastructure.persistence.entity.CategorieEntity;


@Repository
public interface CategorieJpaRepo extends JpaRepository<CategorieEntity, Long> {
}
