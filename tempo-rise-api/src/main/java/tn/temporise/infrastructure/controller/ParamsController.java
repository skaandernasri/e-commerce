package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tn.temporise.application.service.ParamSectionService;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.api.ParamsApi;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ParamsController implements ParamsApi {

  private final ParamSectionService paramSectionService;


  @Override
  public ResponseEntity<ImageUploadResponse> _createImageSection(Long id, MultipartFile contenu)
      throws Exception {
    return ResponseEntity.ok(paramSectionService.uploadImage(id, contenu));
  }



  @Override
  public ResponseEntity<ParamSectionResponse> _createSection(
      ParamSectionRequest paramSectionRequest) throws Exception {
    return ResponseEntity.ok(paramSectionService.createSection(paramSectionRequest));
  }



  @Override
  public ResponseEntity<Void> _deleteImageSection(Long sectionId) throws Exception {
    paramSectionService.deleteImage(sectionId);
    return null;
  }



  @Override
  public ResponseEntity<Response> _deleteSection(Long id) throws Exception {
    return ResponseEntity.ok(paramSectionService.deleteSection(id));
  }

  @Override
  public ResponseEntity<List<ParamSectionResponse>> _getAllSections() throws Exception {
    return ResponseEntity.ok(paramSectionService.getSections());
  }

  @Override
  public ResponseEntity<ParamSectionResponse> _getSectionById(Long id) throws Exception {
    return ResponseEntity.ok(paramSectionService.getSectionById(id));
  }


  @Override
  public ResponseEntity<List<ParamSectionResponse>> _getSectionByPageType(TypePageDto type)
      throws Exception {
    return ResponseEntity.ok(paramSectionService.getSectionsByPageType(type));
  }

  @Override
  public ResponseEntity<List<ParamSectionResponse>> _getSectionByType(TypeSectionDto type)
      throws Exception {
    return ResponseEntity.ok(paramSectionService.getSectionsByType(type));
  }

  @Override
  public ResponseEntity<ParamSectionResponse> _updateSection(Long id,
      ParamSectionRequest paramSectionRequest) throws Exception {
    return ResponseEntity.ok(paramSectionService.updateSection(id, paramSectionRequest));
  }
}
