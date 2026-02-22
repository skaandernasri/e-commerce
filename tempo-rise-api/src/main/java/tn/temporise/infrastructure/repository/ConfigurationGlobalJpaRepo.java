package tn.temporise.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.temporise.infrastructure.persistence.entity.ConfigurationGlobalEntity;

@Repository
public interface ConfigurationGlobalJpaRepo extends JpaRepository<ConfigurationGlobalEntity, Long> {
}
