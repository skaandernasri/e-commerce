package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.temporise.domain.model.Contact;
import tn.temporise.domain.model.ContactCreateRequestDto;
import tn.temporise.domain.model.ContactDto;
import tn.temporise.infrastructure.persistence.entity.ContactEntity;

@Mapper(componentModel = "spring",
    uses = {DateMapper.class, CommandeMapper.class, UserMapper.class})
public interface ContactMapper {
  @Mapping(target = "user.id", source = "userId")
  @Mapping(target = "commande.id", source = "commandeId")
  Contact toContact(ContactCreateRequestDto contactDto);

  @Mapping(target = "commande",
      expression = "java(contact.commande()==null || contact.commande().id()==null ? null : CommandeMapper.INSTANCE.mapIdToCommandeEntity(contact.commande().id()))")
  ContactEntity toContactEntity(Contact contact);

  Contact toContact(ContactEntity contactEntity);

  ContactDto toContactDto(Contact contact);

  default Long toLong(Long id) {
    return id;
  }
}
