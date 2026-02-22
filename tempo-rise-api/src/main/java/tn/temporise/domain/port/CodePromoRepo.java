package tn.temporise.domain.port;

import tn.temporise.domain.model.CodePromo;

import java.time.LocalDateTime;
import java.util.List;

public interface CodePromoRepo {
  CodePromo save(CodePromo codePromo);

  CodePromo findById(Long id);

  List<CodePromo> findAll();

  void deleteById(Long id);

  void deleteAll();

  CodePromo findByCode(String code);

  boolean existsByCode(String code);

  List<CodePromo> findByExpirationDateLessThanEqual(LocalDateTime currentDate);

  List<CodePromo> findByExpirationDateGreaterThan(LocalDateTime currentDate);

}
