package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.ContactService;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.api.ContactApi;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ContactController implements ContactApi {
  private final ContactService contactService;

  @Override
  public ResponseEntity<ContactDto> _createContact(ContactCreateRequestDto contactCreateRequestDto)
      throws Exception {
    return ResponseEntity.ok(contactService.createContact(contactCreateRequestDto));
  }

  @Override
  public ResponseEntity<ContactDto> _getContactById(Integer id) throws Exception {
    return null;
  }

  @Override
  public ResponseEntity<GetContacts200Response> _getContacts(Integer page, Integer size,
      TypeContact type, StatusContact statusContact, RefundMethod refundMethod, SortEnum sort)
      throws Exception {
    return ResponseEntity
        .ok(contactService.getAllContacts(page, size, type, statusContact, refundMethod, sort));
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<Response> _updateContactStatus(Long id,
      ContactStatusUpdateDto contactStatusUpdateDto) throws Exception {
    contactService.updateContact(id, contactStatusUpdateDto);
    Response response = new Response();
    response.setMessage("Contact updated successfully");
    response.setCode("200");
    return ResponseEntity.ok(response);
  }

}
