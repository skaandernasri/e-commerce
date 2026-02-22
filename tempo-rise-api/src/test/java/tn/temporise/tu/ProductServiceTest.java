package tn.temporise.tu;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.service.*;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.application.mapper.ProductMapper;
import tn.temporise.config.ZoneConfig;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

  @Mock
  private ProductRepo productRepo;

  @Mock
  private CategorieRepo categorieRepo;

  @Mock
  private ProductMapper productMapper;

  @Mock
  private UploadImageService uploadImageService;

  @Mock
  private PanierVariantRepo panierVariantRepo;
  @Mock
  private VariantService variantService;
  @Mock
  private VariantRepo variantRepo;
  @Mock
  private ExceptionFactory exceptionFactory;
  @Mock
  private PanierRepo panierRepo;
  @Mock
  private PanierService panierService;
  @Mock
  private ZoneConfig zoneConfig;
  @Mock
  private ImageProductService imageProductService;
  @Mock
  private StockService stockService;
  @InjectMocks
  private ProductService productService;

  private ProductRequest productRequest;
  private Produit produit;
  private ProductResponse productResponse;
  private Categorie categorie = new Categorie(1L, "Electronics", "Gadgets and devices");

  @BeforeEach
  void setUp() {
    VariantRequest variantRequest = new VariantRequest();
    variantRequest.setColor("Red");
    variantRequest.setSize("M");
    variantRequest.setQuantity(10L);
    productRequest = new ProductRequest();
    productRequest.setNom("Smartphone");
    productRequest.setDescription("High-end smartphone");
    productRequest.setPrix(999.99);
    productRequest.setVariants(List.of(variantRequest));
    productRequest.setCategorie(1L);
    produit = Produit.builder().id(1L).nom("Smartphone").description("High-end smartphone")
        .prix(999.99).variants(null).build();
    // productResponse = new ProductResponse(1L, "Smartphone", "High-end smartphone", 999.99, 10,
    // new ProductResponseCategorie());
    productResponse = new ProductResponse();
    productResponse.setId(1L);
    productResponse.setNom("Smartphone");
    productResponse.setDescription("High-end smartphone");
    productResponse.setPrix(999.99);
    productResponse.setVariants(List.of(new VariantResponse()));
    // Mock exception factory responses
  }

  @Test
    void testCreateProduct_Success() {
        when(productMapper.dtoToModel(productRequest)).thenReturn(produit);
        when(productRepo.save(produit)).thenReturn(produit);
        when(productMapper.modelToResponse(produit)).thenReturn(productResponse);
        when(categorieRepo.findById(1L)).thenReturn(categorie);

        ProductResponse response = productService.createProduct(productRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Smartphone", response.getNom());
        verify(productRepo, times(1)).save(produit);
        verify(categorieRepo, times(1)).findById(1L);
    }

  @Test
  void testCreateProduct_NullRequest() {
    assertThrows(NullPointerException.class, () -> productService.createProduct(null));
    verify(exceptionFactory, times(1)).badRequest("invalid_request");
    verify(productRepo, never()).save(any());
  }

  @Test
    void testCreateProduct_CategorieNotFound() {
        when(categorieRepo.findById(1L)).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> productService.createProduct(productRequest));
        verify(categorieRepo, times(1)).findById(1L);
        verify(productRepo, never()).save(any());
    }

  @Test
    void testCreateProduct_InternalError() {
        when(categorieRepo.findById(1L)).thenReturn(categorie);
        when(productMapper.dtoToModel(productRequest)).thenReturn(produit);
        when(productRepo.save(produit)).thenThrow(new RuntimeException("DB Error"));

        assertThrows(NullPointerException.class, () -> productService.createProduct(productRequest));
        verify(exceptionFactory, times(1)).internalServerError("internal.server_error", "DB Error");
    }

  @Test
    void testGetProductById_Success() {
        when(productRepo.findById(1L)).thenReturn(produit);
        when(productMapper.modelToResponse(produit)).thenReturn(productResponse);

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Smartphone", response.getNom());
    }

  @Test
  void testGetProductById_NullId() {
    assertThrows(NullPointerException.class, () -> productService.getProductById(null));
    verify(exceptionFactory, times(1)).badRequest("badrequest.invalid_id");
  }

  @Test
    void testGetProductById_NotFound() {
        when(productRepo.findById(1L)).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> productService.getProductById(1L));
    }

  @Test
  void testUpdateProduct_Success() {
    // Arrange
    Produit existingProduit =
        Produit.builder().id(1L).nom("Old Name").description("Old Description").prix(500.0).build();

    Produit updatedProduit = Produit.builder().id(1L).nom("Smartphone")
        .description("High-end smartphone").prix(999.99).categorie(categorie).build();

    when(productRepo.findById(1L)).thenReturn(existingProduit);
    when(categorieRepo.findById(1L)).thenReturn(categorie);
    when(productMapper.dtoToModel(productRequest)).thenReturn(updatedProduit);
    when(productRepo.save(updatedProduit)).thenReturn(updatedProduit);
    when(productMapper.modelToResponse(any(Produit.class))).thenReturn(productResponse);

    // Act
    ProductResponse response = productService.updateProduct(1L, productRequest);

    // Assert
    assertNotNull(response);
    assertEquals(1L, response.getId());
    assertEquals("Smartphone", response.getNom());
    verify(productRepo, times(1)).save(updatedProduit);
  }

  @Test
  void testUpdateProduct_NullInput() {
    assertThrows(NullPointerException.class,
        () -> productService.updateProduct(null, productRequest));
    assertThrows(NullPointerException.class, () -> productService.updateProduct(1L, null));
    verify(exceptionFactory, times(2)).badRequest("badrequest.invalid_input");
  }

  @Test
    void testUpdateProduct_ProductNotFound() {
        when(productRepo.findById(1L)).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> productService.updateProduct(1L, productRequest));
    }

  @Test
  void testUpdateProduct_CategorieNotFound() {
    Produit existingProduit =
        Produit.builder().id(1L).nom("Old Name").description("Old Description").prix(500.0).build();
    when(productRepo.findById(1L)).thenReturn(existingProduit);
    when(categorieRepo.findById(1L)).thenThrow(NotFoundException.class);

    assertThrows(NotFoundException.class, () -> productService.updateProduct(1L, productRequest));
  }

  @Test
    void testDeleteProduct_Success() {
        when(productRepo.findById(1L)).thenReturn(produit);
        assertDoesNotThrow(() -> productService.deleteProduct(1L));
        verify(productRepo, times(1)).deleteById(1L);
    }

  @Test
  void testDeleteProduct_NullId() {
    assertThrows(NullPointerException.class, () -> productService.deleteProduct(null));
    verify(exceptionFactory, times(1)).badRequest("badrequest.invalid_id");
  }

  @Test
    void testDeleteProduct_NotFound() {
        when(productRepo.findById(1L)).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> productService.deleteProduct(1L));
    }

  @Test
    void testGetAllProducts_Success() {
        when(productRepo.findAll()).thenReturn(List.of(produit));
        when(productMapper.modelToResponse(produit)).thenReturn(productResponse);

        List<ProductResponse> responses = productService.getAllProducts();

        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals("Smartphone", responses.get(0).getNom());
    }

  @Test
    void testGetAllProducts_Empty() {
        when(productRepo.findAll()).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> productService.getAllProducts());
    }

  @Test
    void testDeleteAllProducts_Success() {
        when(productRepo.findAll()).thenReturn(List.of(produit));
        assertDoesNotThrow(() -> productService.deleteAllProducts());
        verify(productRepo, times(1)).deleteAll();
    }

  @Test
    void testDeleteAllProducts_Empty() {
        when(productRepo.findAll()).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> productService.deleteAllProducts());
    }

  @Test
  void getPaginatedProducts() {
    Page<Produit> page = new PageImpl<>(List.of(produit));
    when(productRepo.findAll(any(ProduitFilter.class), any(PageRequest.class))).thenReturn(page);
    when(productMapper.modelToResponse(any(Produit.class))).thenReturn(productResponse);
    GetFilteredProductsPage200Response response = productService.getAllProductPagedAndFiltered(0,
        10, null, null, null, null, null, null, null, null);
    assertNotNull(response);
    assertEquals(1, response.getContent().size());
  }

  @Test
    void paginatedProductsNotFound() {
        when(productRepo.findAll(any(ProduitFilter.class),any(PageRequest.class))).thenReturn(Page.empty());

        GetFilteredProductsPage200Response response = productService.getAllProductPagedAndFiltered(
                0,
                10,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertNotNull(response);
        assertEquals(0, response.getContent().size());
    }
}
