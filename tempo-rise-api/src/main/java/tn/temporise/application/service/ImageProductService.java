package tn.temporise.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.application.mapper.ImageProductMapper;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.ImageProduitRepo;
import tn.temporise.domain.port.ProductRepo;


import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageProductService {
  private final ImageProduitRepo imageProduitRepo;
  private final ImageProductMapper imageProductMapper;
  private final ProductRepo productRepo;
  private final ExceptionFactory exceptionFactory;
  private final UploadImageService uploadImageService;

  @Transactional
  public ImageProduitResponse createImage(Long produitId, MultipartFile image) {
    try {
      if (produitId == null || image == null || image.isEmpty()
          || (image.getOriginalFilename() != null
              && !image.getOriginalFilename().matches(".*\\.(png|jpg|jpeg)$"))) {
        throw exceptionFactory.badRequest("badrequest.invalid_request");
      }

      productRepo.findById(produitId);


      // --- Save file to local folder ---
      ImageUploadResponse imageUploadResponse = uploadImageService.uploadImage(image);

      // --- Map DTO/entity ---
      ImageProduit imageProduit = imageProductMapper.dtoToModel(produitId, image);
      imageProduit = imageProduit.toBuilder().url(imageUploadResponse.getFilename()).build(); // store
                                                                                              // filename
                                                                                              // in
                                                                                              // DB

      ImageProduit savedImage = imageProduitRepo.save(imageProduit);

      log.info("Image created successfully{}", savedImage);
      return imageProductMapper.modelToResponse(savedImage);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Image creation failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Image creation failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }


  public Resource getImageById(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      ImageProduit image = imageProduitRepo.findById(id);
      if (image == null) {
        throw exceptionFactory.notFound("notfound.image");
      }
      return new ByteArrayResource(image.contenu());
    } catch (BadRequestException | NotFoundException e) {
      log.warn("Image retrieval failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Image retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public List<ImageProduitResponse> getAllImages() {
    try {
      List<ImageProduitResponse> imageList =
          imageProduitRepo.findAll().stream().map(imageProductMapper::modelToResponse).toList();
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

  public ImageProduitResponse updateImage(Long id, Long produitId, MultipartFile contenu) {
    try {
      if (id == null || produitId == null || contenu == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }

      ImageProduit existingImage = imageProduitRepo.findById(id);
      if (existingImage == null) {
        throw exceptionFactory.notFound("notfound.image");
      }

      Produit produit = productRepo.findById(produitId);
      if (produit == null) {
        throw exceptionFactory.notFound("notfound.product");
      }

      ImageProduit updatedImage = imageProduitRepo
          .save(ImageProduit.builder().id(id).produit(produit).contenu(contenu.getBytes()).build());
      return imageProductMapper.modelToResponse(updatedImage);
    } catch (BadRequestException | NotFoundException e) {
      log.warn("Image update failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Image update failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  @Transactional
  public void deleteImage(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      ImageProduit imageProduit = imageProduitRepo.findById(id);
      if (imageProduit == null)
        return;
      imageProduitRepo.deleteById(id);
      uploadImageService.deleteImage(imageProduit.url());
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
      List<ImageProduit> imageList = imageProduitRepo.findAll();
      if (imageList.isEmpty()) {
        throw exceptionFactory.notFound("notfound.no_images");
      }
      imageProduitRepo.deleteAll();
      uploadImageService.deleteAllImages();
    } catch (NotFoundException e) {
      log.warn("No images to delete", e);
      throw e;
    } catch (Exception e) {
      log.error("Image bulk deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public List<ImageProduitResponse> getImageByProductId(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      productRepo.findById(id);


      List<ImageProduit> images = imageProduitRepo.findByProduitId(id);
      if (images.isEmpty()) {
        return List.of();
      }
      return images.stream().map(imageProductMapper::modelToResponse).toList();
    } catch (BadRequestException | NotFoundException e) {
      log.warn("Product images retrieval failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Product images retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }
}
