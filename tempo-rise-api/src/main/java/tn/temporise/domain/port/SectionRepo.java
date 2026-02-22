package tn.temporise.domain.port;

import tn.temporise.domain.model.Section;
import tn.temporise.domain.model.TypePage;
import tn.temporise.domain.model.TypeSection;

import java.util.List;

public interface SectionRepo {
  Section save(Section section);

  void deleteAll();

  List<Section> saveAll(List<Section> sections);

  Section findById(Long id);

  void checkAllTypeInactive(TypeSection typeSection, TypePage typePage);

  List<Section> findAll();

  void delete(Long id);

  Section findByTitre(String titre);

  List<Section> findByType(TypeSection type);

  List<Section> findByTypePage(TypePage typePage);
}
