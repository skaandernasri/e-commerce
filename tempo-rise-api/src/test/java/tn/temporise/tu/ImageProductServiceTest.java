package tn.temporise.tu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.service.UploadImageService;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.application.mapper.ImageProductMapper;
import tn.temporise.application.service.ImageProductService;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.ImageProduitRepo;
import tn.temporise.domain.port.ProductRepo;


import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageProductServiceTest {

  @Mock
  private ImageProduitRepo imageProduitRepo;

  @Mock
  private ImageProductMapper imageProductMapper;

  @Mock
  private ProductRepo productRepo;

  @Mock
  private UploadImageService uploadImageService;


  @Mock
  private ExceptionFactory exceptionFactory;

  @InjectMocks
  private ImageProductService imageProductService;

  // private final ImageProduitRequest imageRequest = new ImageProduitRequest();
  private MultipartFile file;
  private Produit produit;
  private ImageProduit imageProduit;

  @BeforeEach
  void setUp() throws IOException {
    File imageFile = new File("src/test/resources/image.png");

    file = new MockMultipartFile("contenu", // form field name
        imageFile.getName(), // original filename
        "image/png", // content type
        new BufferedInputStream(new FileInputStream(imageFile)) // file content
    );
    produit = Produit.builder().id(1L).nom("Test Product").description("Desc").prix(200.0)
        .categorie(null).promotions(null).build();
    imageProduit = ImageProduit.builder().produit(produit).contenu(file.getBytes()).build();

  }

  @Test
    void testCreateImage_ProductNotFound() {
        when(productRepo.findById(1L)).thenThrow(new NotFoundException("Product not found", "404"));
        assertThrows(NotFoundException.class, () -> imageProductService.createImage(1L, file));
    }

  @Test
    void testGetImageById_NotFound() {
        when(imageProduitRepo.findById(1L)).thenReturn(null);
        when(exceptionFactory.notFound("notfound.image")).thenThrow(new NotFoundException("Image not found", "404"));
        assertThrows(NotFoundException.class, () -> imageProductService.getImageById(1L));
    }

  @Test
    void testDeleteImage_NotFound() {
        when(imageProduitRepo.findById(1L)).thenThrow(new NotFoundException("Image not found", "404"));
        assertThrows(NotFoundException.class, () -> imageProductService.deleteImage(1L));
    }

  @Test
    void testCreateImageSuccess() throws IOException {
        when(productRepo.findById(1L)).thenReturn(produit);
        ImageProduit savedImage = ImageProduit.builder()
                .id(1L)
                .produit(produit)
                .contenu(file.getBytes())
                .build();
        ImageProduit imageProduit = ImageProduit.builder()
                .id(1L)
                .produit(produit)
                .contenu(file.getBytes())
                .url("test")
                .build();
        ImageUploadResponse imageUploadResponse = new ImageUploadResponse();
        imageUploadResponse.setFilename("test");
        when(uploadImageService.uploadImage(file)).thenReturn(imageUploadResponse);
        when(imageProductMapper.dtoToModel(produit.id(), file)).thenReturn(imageProduit);
        when(imageProduitRepo.save(imageProduit)).thenReturn(savedImage);
        when(imageProductMapper.modelToResponse(savedImage)).thenReturn(any(ImageProduitResponse.class));
        imageProductService.createImage(1L, file);
    }

  @Test
    void testUpdateImageSuccess() throws IOException {
        when(imageProduitRepo.findById(1L)).thenReturn(imageProduit);
        when(productRepo.findById(1L)).thenReturn(produit);
        ImageProduit updatedImage = ImageProduit.builder()
                .id(1L)
                .produit(produit)
                .contenu(file.getBytes())
                .build();
        when(imageProduitRepo.save(updatedImage)).thenReturn(updatedImage);
        when(imageProductMapper.modelToResponse(updatedImage)).thenReturn(any(ImageProduitResponse.class));
        assertDoesNotThrow(() -> imageProductService.updateImage(1L, 1L, file));
    }
}
