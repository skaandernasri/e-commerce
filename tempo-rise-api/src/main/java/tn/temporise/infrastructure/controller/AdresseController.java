package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.AdresseService;
import tn.temporise.domain.model.AdresseRequest;
import tn.temporise.domain.model.AdresseResponse;
import tn.temporise.domain.model.Response;
import tn.temporise.infrastructure.api.AdressesApi;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AdresseController implements AdressesApi {
  private final AdresseService adresseService;

  @Override
  public ResponseEntity<AdresseResponse> _createAdresse(AdresseRequest adresseRequest)
      throws Exception {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(adresseService.createAdresse(adresseRequest));
  }

  @Override
  public ResponseEntity<Response> _deleteAdresse(Long id) throws Exception {
    adresseService.deleteAdresse(id);
    Response response = new Response();
    response.setCode("200");
    response.setMessage("Adresse supprimé avec succès !");
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Response> _deleteAllAdresses() throws Exception {
    adresseService.deleteAllAdresses();
    Response response = new Response();
    response.setCode("200");
    response.setMessage("Toutes les adresses supprimées avec succès !");
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<AdresseResponse> _getAdresseById(Long id) throws Exception {
    return ResponseEntity.ok(adresseService.getAdresseById(id));
  }

  @Override
  public ResponseEntity<List<AdresseResponse>> _getAdresseByType(String type) throws Exception {
    return ResponseEntity.ok(adresseService.getAdressesByType(type));
  }

  @Override
  public ResponseEntity<List<AdresseResponse>> _getAdresseByUserId(Long userId) throws Exception {
    return ResponseEntity.ok(adresseService.getAdressesByUtilisateurId(userId));
  }

  @Override
  public ResponseEntity<List<AdresseResponse>> _getAllAdresses() throws Exception {
    return ResponseEntity.ok(adresseService.getAllAdresses());
  }

  @Override
  public ResponseEntity<AdresseResponse> _updateAdresse(Long id, AdresseRequest adresseRequest)
      throws Exception {
    return ResponseEntity.ok(adresseService.updateAdresse(id, adresseRequest));
  }
}
