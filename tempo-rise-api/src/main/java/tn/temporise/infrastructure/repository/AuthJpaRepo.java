package tn.temporise.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.temporise.infrastructure.persistence.entity.AuthentificationEntity;

import java.util.List;
import java.util.Optional;


@Repository
public interface AuthJpaRepo extends JpaRepository<AuthentificationEntity, Long> {
  Optional<List<AuthentificationEntity>> findByUser_Email(String email);

  Optional<AuthentificationEntity> findByUser_EmailAndProviderId(String email, String providerId);

  Optional<AuthentificationEntity> findByRefreshToken(String token);
}
