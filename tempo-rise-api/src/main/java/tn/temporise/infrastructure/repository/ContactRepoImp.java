package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.ContactMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.Contact;
import tn.temporise.domain.model.ContactFilter;
import tn.temporise.domain.port.ContactRepo;
import tn.temporise.infrastructure.persistence.entity.ContactEntity;

import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ContactRepoImp implements ContactRepo {
  private final ContactJpaRepo contactJpaRepo;
  private final ContactMapper contactMapper;
  private final ExceptionFactory exceptionFactory;

  @Override
  public Contact save(Contact contact) {
    return contactMapper.toContact(contactJpaRepo.save(contactMapper.toContactEntity(contact)));
  }

  @Override
  public Contact findById(Long id) {
    return contactMapper.toContact(contactJpaRepo.findById(id)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.contact")));
  }

  @Override
  public Page<Contact> findAll(ContactFilter filter, Pageable pageable) {
    Specification<ContactEntity> spec = Specification.where(null);
    if (filter.type() != null) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), filter.type()));
    }
    if (filter.statusContact() != null) {
      spec = spec
          .and((root, query, cb) -> cb.equal(root.get("statusContact"), filter.statusContact()));
    }
    if (filter.refundMethod() != null) {
      spec =
          spec.and((root, query, cb) -> cb.equal(root.get("refundMethod"), filter.refundMethod()));
    }
    return contactJpaRepo.findAll(spec, pageable).map(contactMapper::toContact);
  }


  @Override
  public List<Contact> findByType(String type) {
    return contactJpaRepo.findByType(type).stream().map(contactMapper::toContact).toList();
  }

  @Override
  public void deleteById(Long id) {
    contactJpaRepo.deleteById(id);
  }

  @Override
  public void deleteAll() {
    contactJpaRepo.deleteAll();
  }
}
