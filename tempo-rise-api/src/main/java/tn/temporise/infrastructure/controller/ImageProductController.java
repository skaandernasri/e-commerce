package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tn.temporise.application.service.ImageProductService;
import tn.temporise.domain.model.ImageProduitResponse;
import tn.temporise.domain.model.Response;
import tn.temporise.infrastructure.api.ImageProduitApi;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ImageProductController implements ImageProduitApi {
  private final ImageProductService imageProductService;

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<ImageProduitResponse> _createImageProduit(Long produitId,
      MultipartFile contenu) throws Exception {
    ImageProduitResponse imageProduit = imageProductService.createImage(produitId, contenu);
    return ResponseEntity.status(HttpStatus.CREATED).body(imageProduit);
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<Response> _deleteAllImages() throws Exception {
    imageProductService.deleteAllImages();
    Response response = new Response();
    response.setCode("200");
    response.setMessage("Toutes les images ont été supprimés avec succès");
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<Response> _deleteImageProduit(Long id) throws Exception {
    imageProductService.deleteImage(id);
    Response responseBody = new Response();
    responseBody.setCode("200");
    responseBody.setMessage("Image supprimé !");
    return ResponseEntity.ok(responseBody);
  }

  @Override
  public ResponseEntity<List<ImageProduitResponse>> _getAllImagesProduits() throws Exception {
    List<ImageProduitResponse> responses = imageProductService.getAllImages();
    return ResponseEntity.ok(responses);
  }

  @Override
  public ResponseEntity<Resource> _getImageById(Long id) throws Exception {
    Resource resource = imageProductService.getImageById(id);
    return ResponseEntity.ok(resource);
  }

  @Override
  public ResponseEntity<List<ImageProduitResponse>> _getImageByProductId(Long id) throws Exception {
    List<ImageProduitResponse> imageProduitResponse = imageProductService.getImageByProductId(id);
    return ResponseEntity.ok(imageProduitResponse);
  }


  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<ImageProduitResponse> _updateImageProduit(Long id, Long produitId,
      MultipartFile contenu) throws Exception {
    ImageProduitResponse imageProduit = imageProductService.updateImage(id, produitId, contenu);
    return ResponseEntity.ok(imageProduit);
  }
}
