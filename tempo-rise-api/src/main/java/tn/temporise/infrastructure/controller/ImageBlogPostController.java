package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tn.temporise.application.service.ImageBlogService;
import tn.temporise.domain.model.ImageBlogPostResponse;
import tn.temporise.domain.model.Response;
import tn.temporise.infrastructure.api.ImageBlogPostApi;

import java.util.List;


@RestController
@Slf4j
@RequiredArgsConstructor
public class ImageBlogPostController implements ImageBlogPostApi {
  private final ImageBlogService imageBlogService;

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<ImageBlogPostResponse> _creatImageblogpost(MultipartFile image,
      Long blogPostId) throws Exception {
    ImageBlogPostResponse imageBlogPostResponse = imageBlogService.createImage(image, blogPostId);
    return ResponseEntity.status(HttpStatus.CREATED).body(imageBlogPostResponse);
  }

  @Override
  public ResponseEntity<List<ImageBlogPostResponse>> _getAllImagesBlogPost() throws Exception {
    List<ImageBlogPostResponse> responses = imageBlogService.getAllImages();
    return ResponseEntity.ok(responses);
  }


  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<Response> _deleteAllImagesBlogPost() throws Exception {
    imageBlogService.deleteAllImages();
    Response response = new Response();
    response.setCode("200");
    response.setMessage("Toutes les images ont été supprimés avec succès");
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<Response> _deleteImageBlogPost(Long id) throws Exception {
    imageBlogService.deleteImage(id);
    Response responseBody = new Response();
    responseBody.setCode("200");
    responseBody.setMessage("Image supprimé !");
    return ResponseEntity.ok(responseBody);
  }



  @Override
  public ResponseEntity<ImageBlogPostResponse> _getImageBlogPostById(Long id) throws Exception {
    ImageBlogPostResponse imageBlogPostResponse = imageBlogService.getImageById(id);
    return ResponseEntity.ok(imageBlogPostResponse);
  }

  @Override
  public ResponseEntity<List<ImageBlogPostResponse>> _getImagesByBlogPostId(Long id)
      throws Exception {
    List<ImageBlogPostResponse> ImageBlogPostResponse = imageBlogService.getImageByProductId(id);
    return ResponseEntity.ok(ImageBlogPostResponse);
  }


  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @Override
  public ResponseEntity<ImageBlogPostResponse> _updateImageBlogPost(Long id, MultipartFile image,
      Long blogPostId) throws Exception {
    ImageBlogPostResponse imageBlogPostResponse = imageBlogService.updateImage(image, blogPostId);
    return ResponseEntity.ok(imageBlogPostResponse);
  }

}
