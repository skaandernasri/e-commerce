package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tn.temporise.application.service.UploadImageService;
import tn.temporise.domain.model.ImageUploadResponse;
import tn.temporise.domain.model.Response;
import tn.temporise.infrastructure.api.ImagesApi;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ImageUploadController implements ImagesApi {
  private final UploadImageService uploadImageService;

  @Override
  public ResponseEntity<Response> _deleteImage(String filename) throws Exception {
    uploadImageService.deleteImage(filename);
    Response response = new Response();
    response.setMessage("image deleted");
    response.setCode("200");
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<ImageUploadResponse> _uploadImage(MultipartFile file) throws Exception {
    return ResponseEntity.ok(uploadImageService.uploadImage(file));
  }

}
