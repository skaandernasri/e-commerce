package tn.temporise.domain.port;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.temporise.domain.model.Contact;
import tn.temporise.domain.model.ContactFilter;

import java.util.List;

public interface ContactRepo {
  Contact save(Contact contact);

  Contact findById(Long id);

  Page<Contact> findAll(ContactFilter filter, Pageable pageable);

  List<Contact> findByType(String type);

  void deleteById(Long id);

  void deleteAll();

}
