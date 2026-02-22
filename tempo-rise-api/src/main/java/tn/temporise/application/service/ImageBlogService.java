package tn.temporise.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.ImageBlogMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.BlogPostRepo;
import tn.temporise.domain.port.ImageBlogRepo;


import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageBlogService {
  private final ImageBlogRepo imageBlogRepo;
  private final ImageBlogMapper imageBlogMapper;
  private final BlogPostRepo blogPostRepo;
  private final ExceptionFactory exceptionFactory;
  private final UploadImageService uploadImageService;
  @Value("${uploadDir}")
  private String uploadDire;

  @Transactional
  public ImageBlogPostResponse createImage(MultipartFile image, Long blogPostId) {
    try {
      if (blogPostId == null || image == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_request");
      }

      blogPostRepo.findById(blogPostId);
      // --- Save file to local folder ---
      ImageUploadResponse imageUploadResponse = uploadImageService.uploadImage(image);
      ImageBlogPost imageBlogPost = imageBlogMapper.dtoToModel(image, blogPostId);
      imageBlogPost = imageBlogPost.toBuilder().url(imageUploadResponse.getFilename()).build();
      ImageBlogPost savedImage = imageBlogRepo.save(imageBlogPost);
      log.info("Image created successfully{}", savedImage);
      return imageBlogMapper.modelToResponse(savedImage);
    } catch (BadRequestException | NotFoundException e) {
      log.warn("Image creation failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Image creation failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public ImageBlogPostResponse getImageById(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      ImageBlogPost image = imageBlogRepo.findById(id);
      if (image == null) {
        throw exceptionFactory.notFound("notfound.image");
      }
      return imageBlogMapper.modelToResponse(image);
    } catch (BadRequestException | NotFoundException e) {
      log.warn("Image retrieval failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Image retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public List<ImageBlogPostResponse> getAllImages() {
    try {
      List<ImageBlogPostResponse> imageList =
          imageBlogRepo.findAll().stream().map(imageBlogMapper::modelToResponse).toList();
      if (imageList.isEmpty()) {
        throw exceptionFactory.notFound("notfound.no_images");
      }
      return imageList;
    } catch (NotFoundException e) {
      log.warn("No images found", e);
      throw e;
    } catch (Exception e) {
      log.error("Image list retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  @Transactional
  public void deleteImage(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      ImageBlogPost imageBlogPost = imageBlogRepo.findById(id);
      log.info("ImageBlogPost {}", imageBlogPost);
      imageBlogRepo.deleteById(id);
      uploadImageService.deleteImage(imageBlogPost.url());
    } catch (BadRequestException | NotFoundException e) {
      log.warn("Image deletion failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Image deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  @Transactional
  public void deleteAllImages() {
    try {
      List<ImageBlogPost> imageList = imageBlogRepo.findAll();
      if (imageList.isEmpty()) {
        throw exceptionFactory.notFound("notfound.no_images");
      }
      imageBlogRepo.deleteAll();
      uploadImageService.deleteAllImages();
    } catch (NotFoundException e) {
      log.warn("No images to delete", e);
      throw e;
    } catch (Exception e) {
      log.error("Image bulk deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public List<ImageBlogPostResponse> getImageByProductId(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      if (blogPostRepo.findById(id) == null) {
        throw exceptionFactory.notFound("notfound.product");
      }

      List<ImageBlogPost> images = imageBlogRepo.findByProduitId(id);
      if (images.isEmpty()) {
        throw exceptionFactory.notFound("notfound.no_images");
      }
      return images.stream().map(imageBlogMapper::modelToResponse).toList();
    } catch (BadRequestException | NotFoundException e) {
      log.warn("Product images retrieval failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Product images retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public ImageBlogPostResponse updateImage(MultipartFile image, Long blogPostId) {
    try {
      if (blogPostId == null || image == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }

      ImageBlogPost existingImage = imageBlogRepo.findById(blogPostId);
      if (existingImage == null) {
        throw exceptionFactory.notFound("notfound.image");
      }

      BlogPost blogPost = blogPostRepo.findById(blogPostId);
      if (blogPost == null) {
        throw exceptionFactory.notFound("notfound.product");
      }

      ImageBlogPost updatedImage = imageBlogRepo
          .save(ImageBlogPost.builder().id(blogPostId).image(image.getBytes()).build());
      return imageBlogMapper.modelToResponse(updatedImage);
    } catch (BadRequestException | NotFoundException e) {
      log.warn("Image update failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Image update failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }
}
