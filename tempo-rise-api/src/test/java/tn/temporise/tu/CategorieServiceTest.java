package tn.temporise.tu;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.server.InternalServerErrorException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.application.mapper.CategorieMapper;
import tn.temporise.application.service.CategorieService;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.CategorieRepo;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CategorieServiceTest {

  @Mock
  private CategorieRepo categorieRepo;

  @Mock
  private CategorieMapper categorieMapper;

  @Mock
  private ExceptionFactory exceptionFactory;

  @InjectMocks
  private CategorieService categorieService;

  private CategorieRequest categorieRequest = new CategorieRequest();
  private CategorieResponse categorieResponse = new CategorieResponse();
  private Categorie categorie;

  @BeforeEach
  void setUp() {
    // Setup test data
    categorie = new Categorie(1L, "Electronics", "Gadgets and devices");

    categorieRequest = new CategorieRequest();
    categorieRequest.setNom("Electronics");
    categorieRequest.setDescription("Gadgets and devices");

    categorieResponse = new CategorieResponse();


  }

  @Test
    void testCreateCategorie_Success() {
        when(categorieMapper.dtoToModel(categorieRequest)).thenReturn(categorie);
        when(categorieRepo.save(categorie)).thenReturn(categorie);
        when(categorieMapper.modelToResponse(categorie)).thenReturn(categorieResponse);

        CategorieResponse response = categorieService.createCategorie(categorieRequest);

        assertNotNull(response);
        log.info("Response: {}", response);
        verify(categorieRepo, times(1)).save(categorie);
    }

  @Test
    void testCreateCategorie_NullRequest() {
        when(exceptionFactory.badRequest("badrequest.invalid_input"))
                .thenReturn(new BadRequestException("Invalid input","4007"));

        assertThrows(BadRequestException.class, () -> categorieService.createCategorie(null));
        verify(exceptionFactory, times(1)).badRequest("badrequest.invalid_input");
    }

  @Test
    void testCreateCategorie_SaveFailed() {
        when(categorieMapper.dtoToModel(categorieRequest)).thenReturn(categorie);
        when(categorieRepo.save(categorie)).thenReturn(null);
        when(exceptionFactory.internalServerError("internal.server_error", "Category creation failed"))
                .thenReturn(new InternalServerErrorException("Internal server error", "5000"));

        assertThrows(RuntimeException.class, () -> categorieService.createCategorie(categorieRequest));
        verify(exceptionFactory, times(1)).internalServerError("internal.server_error", "Category creation failed");
    }

  @Test
    void testGetCategorieById_Success() {
        when(categorieRepo.findById(1L)).thenReturn(categorie);
        when(categorieMapper.modelToResponse(categorie)).thenReturn(categorieResponse);

        CategorieResponse response = categorieService.getCategorieById(1L);
        assertNotNull(response);
    }

  @Test
    void testGetCategorieById_NullId() {
        when(exceptionFactory.badRequest("badrequest.invalid_id"))
                .thenReturn(new BadRequestException("Invalid ID", "4000"));

        assertThrows(BadRequestException.class, () -> categorieService.getCategorieById(null));
        verify(exceptionFactory, times(1)).badRequest("badrequest.invalid_id");
    }

  @Test
    void testGetCategorieById_NotFound() {
        when(categorieRepo.findById(1L)).thenReturn(null);
        when(exceptionFactory.notFound("notfound.category"))
                .thenReturn(new NotFoundException("Category not found", "404"));

        assertThrows(NotFoundException.class, () -> categorieService.getCategorieById(1L));
        verify(exceptionFactory, times(1)).notFound("notfound.category");
    }

  @Test
    void testGetAllCategories_Success() {
        when(categorieRepo.findAll()).thenReturn(List.of(categorie));
        when(categorieMapper.modelToResponse(categorie)).thenReturn(categorieResponse);

        List<CategorieResponse> responses = categorieService.getAllCategories();

        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
    }

  @Test
    void testGetAllCategories_Empty() {
        when(categorieRepo.findAll()).thenReturn(Collections.emptyList());
        when(exceptionFactory.notFound("notfound.no_categories"))
                .thenReturn(new NotFoundException("No categories found", "404"));

        assertThrows(NotFoundException.class, () -> categorieService.getAllCategories());
        verify(exceptionFactory, times(1)).notFound("notfound.no_categories");
    }

  @Test
    void testUpdateCategorie_Success() {
        when(categorieRepo.findById(1L)).thenReturn(categorie);
        categorieRequest.setId(1L);
        when(categorieMapper.dtoToModel(categorieRequest)).thenReturn(categorie);
        when(categorieRepo.update(categorie)).thenReturn(categorie);
        when(categorieMapper.modelToResponse(categorie)).thenReturn(categorieResponse);

        CategorieResponse response = categorieService.updateCategorie(1L, categorieRequest);
        System.out.println("response id "+response.getId());
        assertNotNull(response);
    }

  @Test
    void testUpdateCategorie_NullInput() {
        when(exceptionFactory.badRequest("badrequest.invalid_input"))
                .thenReturn(new BadRequestException("Invalid input", "4000"));

        assertThrows(BadRequestException.class, () -> categorieService.updateCategorie(null, categorieRequest));
        assertThrows(BadRequestException.class, () -> categorieService.updateCategorie(1L, null));
        verify(exceptionFactory, times(2)).badRequest("badrequest.invalid_input");
    }

  @Test
    void testUpdateCategorie_NotFound() {
        when(categorieRepo.findById(1L)).thenReturn(null);
        when(exceptionFactory.notFound("notfound.category"))
                .thenReturn(new NotFoundException("Category not found", "404"));

        assertThrows(NotFoundException.class, () -> categorieService.updateCategorie(1L, categorieRequest));
        verify(exceptionFactory, times(1)).notFound("notfound.category");
    }

  @Test
    void testUpdateCategorie_UpdateFailed() {
        when(categorieRepo.findById(1L)).thenReturn(categorie);
        categorieRequest.setId(1L);
        when(categorieMapper.dtoToModel(categorieRequest)).thenReturn(categorie);
        when(categorieRepo.update(categorie)).thenReturn(null);
        when(exceptionFactory.internalServerError("internal.server_error", "Category update failed"))
                .thenReturn(new InternalServerErrorException("Internal server error", "5000"));

        assertThrows(RuntimeException.class, () -> categorieService.updateCategorie(1L, categorieRequest));
        verify(exceptionFactory, times(1)).internalServerError("internal.server_error", "Category update failed");
    }

  @Test
    void testDeleteCategorie_Success() {
        when(categorieRepo.findById(1L)).thenReturn(categorie);
        assertDoesNotThrow(() -> categorieService.deleteCategorie(1L));
        verify(categorieRepo, times(1)).deleteById(1L);
    }

  @Test
    void testDeleteCategorie_NullId() {
        when(exceptionFactory.badRequest("badrequest.invalid_id"))
                .thenReturn(new BadRequestException("Invalid ID", "4000"));

        assertThrows(BadRequestException.class, () -> categorieService.deleteCategorie(null));
        verify(exceptionFactory, times(1)).badRequest("badrequest.invalid_id");
    }

  @Test
    void testDeleteCategorie_NotFound() {
        when(categorieRepo.findById(1L)).thenReturn(null);
        when(exceptionFactory.notFound("notfound.category"))
                .thenReturn(new NotFoundException("Category not found", "404"));

        assertThrows(NotFoundException.class, () -> categorieService.deleteCategorie(1L));
        verify(exceptionFactory, times(1)).notFound("notfound.category");
    }

  @Test
    void testDeleteAllCategories_Success() {
        when(categorieRepo.findAll()).thenReturn(List.of(categorie));
        assertDoesNotThrow(() -> categorieService.deleteAllCategories());
        verify(categorieRepo, times(1)).deleteAll();
    }

  @Test
    void testDeleteAllCategories_Empty() {
        when(categorieRepo.findAll()).thenReturn(Collections.emptyList());
        when(exceptionFactory.notFound("notfound.no_categories"))
                .thenReturn(new NotFoundException("No categories found", "404"));

        assertThrows(NotFoundException.class, () -> categorieService.deleteAllCategories());
        verify(exceptionFactory, times(1)).notFound("notfound.no_categories");
    }
}
