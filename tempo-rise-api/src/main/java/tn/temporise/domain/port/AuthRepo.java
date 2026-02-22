package tn.temporise.domain.port;


import tn.temporise.domain.model.Authentification;

import java.util.List;

public interface AuthRepo {
  List<Authentification> findByUserEmail(String email);

  Authentification findByUserEmailAndProviderId(String email, String providerId);

  Authentification findByRefreshToken(String token);

  Authentification save(Authentification authentification);

  void deleteAll();

  void deleteById(Long id);
}

