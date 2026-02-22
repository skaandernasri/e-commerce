package tn.temporise.domain.port;

import tn.temporise.domain.model.Role;
import tn.temporise.domain.model.UtilisateurModel;

import java.util.List;
import java.util.Set;

public interface UserRepo {
  UtilisateurModel findByEmail(String email);

  Set<Role> findRolesById(long id);

  void deleteByEmail(String email);

  UtilisateurModel save(UtilisateurModel utilisateurModel);

  UtilisateurModel findById(Long id);

  UtilisateurModel findAllTypeOfUserById(Long id);

  void deleteAll();

  UtilisateurModel findByActivationToken(String token); // activation token

  UtilisateurModel findByResetPasswordToken(String token);

  UtilisateurModel update(UtilisateurModel utilisateurModel);

  List<UtilisateurModel> findAll(); // List<Utilisateur>

  void deleteById(Long id);

  List<UtilisateurModel> findByRole(Role role);
}

