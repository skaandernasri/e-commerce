package tn.temporise.infrastructure.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.temporise.domain.model.Role;
import tn.temporise.domain.model.UserType;
import tn.temporise.infrastructure.persistence.entity.UtilisateurEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserJpaRepo extends JpaRepository<UtilisateurEntity, Long> {
  @Query("SELECT u FROM UtilisateurEntity u WHERE u.email = :email AND u.userType = :userType")
  Optional<UtilisateurEntity> findByEmail(String email, @Param("userType") UserType userType);

  @Query("SELECT u.roles FROM UtilisateurEntity u WHERE u.id = :id AND u.userType = :userType")
  Set<Role> findRolesById(long id, @Param("userType") UserType userType);

  @Modifying
  @Transactional
  @Query("DELETE FROM UtilisateurEntity u WHERE u.email = :email AND u.userType = :userType")
  void deleteByEmail(String email, @Param("userType") UserType userType);

  @Query("SELECT u FROM UtilisateurEntity u WHERE u.id = :id AND u.userType = :userType")
  Optional<UtilisateurEntity> findById(@Param("id") Long id, @Param("userType") UserType userType);

  @Modifying
  @Transactional
  @Query("DELETE FROM UtilisateurEntity u WHERE u.id = :id AND u.userType = :userType")
  void deleteById(@Param("id") Long id, @Param("userType") UserType userType);

  @Query("SELECT u FROM UtilisateurEntity u WHERE u.activation_token = :token AND u.userType = :userType")
  Optional<UtilisateurEntity> findByActivationToken(@Param("token") String token,
      @Param("userType") UserType userType);

  @Query("SELECT u FROM UtilisateurEntity u WHERE u.resetpasswordtoken = :token AND u.userType = :userType")
  Optional<UtilisateurEntity> findbyResetPasswordToken(@Param("token") String token,
      @Param("userType") UserType userType);

  @Query("SELECT u.isverified FROM UtilisateurEntity u WHERE u.id = :id AND u.userType = :userType")
  Boolean isUserActive(@Param("id") Long id, @Param("userType") UserType userType);

  @Query("SELECT u FROM UtilisateurEntity u JOIN u.roles r WHERE r = :role AND u.userType = :userType")
  List<UtilisateurEntity> findByRole(@Param("role") Role role,
      @Param("userType") UserType userType);

  @Modifying
  @Transactional
  @Query("DELETE FROM UtilisateurEntity u WHERE u.userType = :userType")
  void deleteAll(@Param("userType") UserType userType);

  @Query("SELECT u FROM UtilisateurEntity u WHERE u.userType = :userType")
  List<UtilisateurEntity> findAll(@Param("userType") UserType userType);

}
