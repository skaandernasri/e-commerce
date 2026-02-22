package tn.temporise.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import tn.temporise.infrastructure.persistence.entity.ContactEntity;

import java.util.List;

@Repository
public interface ContactJpaRepo
    extends JpaRepository<ContactEntity, Long>, JpaSpecificationExecutor<ContactEntity> {
  List<ContactEntity> findByType(String type);
}
