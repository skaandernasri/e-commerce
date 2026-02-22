package tn.temporise.tu;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.application.mapper.AvisMapper;
import tn.temporise.application.service.AvisService;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.AvisRepo;
import tn.temporise.domain.port.ProductRepo;
import tn.temporise.domain.port.UserRepo;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class AvisServiceTest {

  @Mock
  private AvisRepo avisRepo;

  @Mock
  private AvisMapper avisMapper;

  @Mock
  private UserRepo userRepo;

  @Mock
  private ProductRepo productRepo;

  @Mock
  private ExceptionFactory exceptionFactory;

  @InjectMocks
  private AvisService avisService;

  private AvisRequest avisRequest;
  private Avis avis;
  private AvisResponse avisResponse;
  private UtilisateurModel utilisateur;
  private Produit produit;

  @BeforeEach
  void setUp() {
    // Setup test data
    utilisateur = UtilisateurModel.builder().id(1L).nom("user").email("email@gmail.com")
        .password("Password123").roles(Set.of(Role.ADMIN)).build();
    produit = Produit.builder().id(1L)
        .categorie(Categorie.builder().id(1L).nom("category").description("description").build())
        .prix(10.0).nom("product").description("description").build();

    avisRequest = new AvisRequest();
    avisRequest.setNote(5);
    avisRequest.setCommentaire("Great product!");
    avisRequest.setProduitId(1L);
    avisRequest.setUtilisateurId(1L);
    avisRequest.setDatePublication(OffsetDateTime.parse("2023-07-20T14:30:00Z"));

    avis = Avis.builder().id(1L).note(5).commentaire("Great product!")
        // .datePublication(new Timestamp(System.currentTimeMillis())
        .utilisateur(utilisateur).produit(produit).build();

    avisResponse = new AvisResponse();
    avisResponse.setId(1L);
    avisResponse.setNote(5);
    avisResponse.setCommentaire("Great product!");


  }

  @Test
    void testCreateAvis_Success() {
        when(userRepo.findById(1L)).thenReturn(utilisateur);
        log.info("before productRepo save");
        when(productRepo.findById(1L)).thenReturn(produit);
        log.info("after productRepo save");
        when(avisMapper.dtoToModel(avisRequest)).thenReturn(avis);
        when(avisRepo.save(avis)).thenReturn(avis);
        when(avisMapper.modelToResponse(avis)).thenReturn(avisResponse);
        AvisResponse result = avisService.createAvis(avisRequest);

        assertNotNull(result);
        assertEquals(avisResponse.getId(), result.getId());
        verify(avisRepo, times(1)).save(avis);
    }

  @Test
    void testCreateAvis_NullRequest() {
        when(exceptionFactory.badRequest("badrequest.invalid_request"))
                .thenReturn(new BadRequestException("Invalid request", "4000"));

        assertThrows(BadRequestException.class, () -> avisService.createAvis(null));
        verify(exceptionFactory, times(1)).badRequest("badrequest.invalid_request");
    }

  @Test
    void testCreateAvis_UserNotFound() {
        when(userRepo.findById(1L)).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> avisService.createAvis(avisRequest));
        verify(userRepo,times(1)).findById(1L);
        verify(productRepo, never()).findById(any());
      //  verify(exceptionFactory, times(1)).notFound("notfound.user");
    }

  @Test
    void testCreateAvis_ProductNotFound() {
        when(userRepo.findById(1L)).thenReturn(utilisateur);
        when(productRepo.findById(1L)).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> avisService.createAvis(avisRequest));
        verify(avisMapper, never()).dtoToModel(avisRequest);
    }

  @Test
    void testGetAvisById_Success() {
        when(avisRepo.findById(1L)).thenReturn(avis);
        when(avisMapper.modelToResponse(avis)).thenReturn(avisResponse);

        AvisResponse result = avisService.getAvisById(1L);

        assertNotNull(result);
        assertEquals(avisResponse.getId(), result.getId());
    }

  @Test
    void testGetAvisById_NullId() {
        when(exceptionFactory.badRequest("badrequest.invalid_id"))
                .thenReturn(new BadRequestException("Invalid ID", "4000"));

        assertThrows(BadRequestException.class, () -> avisService.getAvisById(null));
        verify(exceptionFactory, times(1)).badRequest("badrequest.invalid_id");
    }

  @Test
    void testGetAvisById_NotFound() {
        when(avisRepo.findById(1L)).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> avisService.getAvisById(1L));
    }

  @Test
    void testGetAllAvis_Success() {
        when(avisRepo.findAll()).thenReturn(List.of(avis));
        when(avisMapper.modelToResponse(avis)).thenReturn(avisResponse);

        List<AvisResponse> result = avisService.getAllAvis();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

  @Test
    void testUpdateAvis_Success() {
        when(avisRepo.findById(1L)).thenReturn(avis);
        when(avisMapper.dtoToModel(avisRequest)).thenReturn(avis);
        when(avisRepo.save(avis)).thenReturn(avis);
        when(avisMapper.modelToResponse(avis)).thenReturn(avisResponse);

        AvisResponse result = avisService.updateAvis(1L, avisRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

  @Test
    void testUpdateAvis_NullId() {
        when(exceptionFactory.badRequest("badrequest.invalid_id"))
                .thenReturn(new BadRequestException("Invalid ID", "4000"));

        assertThrows(BadRequestException.class, () -> avisService.updateAvis(null, avisRequest));
        verify(exceptionFactory, times(1)).badRequest("badrequest.invalid_id");
    }

  @Test
    void testUpdateAvis_NotFound() {
        when(avisRepo.findById(1L)).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> avisService.updateAvis(1L, avisRequest));
    }

  @Test
    void testDeleteAvis_Success() {
        when(avisRepo.findById(1L)).thenReturn(avis);
        assertDoesNotThrow(() -> avisService.deleteAvis(1L));
        verify(avisRepo, times(1)).deleteById(1L);
    }

  @Test
    void testDeleteAvis_NullId() {
        when(exceptionFactory.badRequest("badrequest.invalid_id"))
                .thenReturn(new BadRequestException("Invalid ID", "4000"));

        assertThrows(BadRequestException.class, () -> avisService.deleteAvis(null));
        verify(exceptionFactory, times(1)).badRequest("badrequest.invalid_id");
    }

  @Test
    void testDeleteAvis_NotFound() {
        when(avisRepo.findById(1L)).thenThrow(NotFoundException.class);
//        when(exceptionFactory.notFound("notfound.avis"))
//                .thenReturn(new NotFoundException("Avis not found", "404"));

        assertThrows(NotFoundException.class, () -> avisService.deleteAvis(1L));
//        verify(exceptionFactory, times(1)).notFound("notfound.avis");
    }

  @Test
    void testGetAvisByProduitId_Success() {
        when(productRepo.findById(1L)).thenReturn(produit);
        when(avisRepo.findByProduitId(1L)).thenReturn(List.of(avis));
        when(avisMapper.modelToResponse(avis)).thenReturn(avisResponse);

        List<AvisResponse> result = avisService.getAvisByProduitId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

  @Test
    void testGetAvisByProduitId_ProductNotFound() {
        when(productRepo.findById(1L)).thenThrow(NotFoundException.class);


        assertThrows(NotFoundException.class, () -> avisService.getAvisByProduitId(1L));
    }

  @Test
    void testGetAvisByProduitId_NoAvis() {
        when(productRepo.findById(1L)).thenReturn(produit);
        when(avisRepo.findByProduitId(1L)).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> avisService.getAvisByProduitId(1L));
    }

  @Test
    void testGetAvisByUtilisateurId_Success() {
        when(userRepo.findById(1L)).thenReturn(utilisateur);
        when(avisRepo.findByUtilisateurId(1L)).thenReturn(List.of(avis));
        when(avisMapper.modelToResponse(avis)).thenReturn(avisResponse);

        List<AvisResponse> result = avisService.getAvisByUtilisateurId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

  @Test
    void testGetAvisByUtilisateurId_UserNotFound() {
        when(userRepo.findById(1L)).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> avisService.getAvisByUtilisateurId(1L));
    }

  @Test
    void testGetAvisByUtilisateurId_NoAvis() {
        when(userRepo.findById(1L)).thenReturn(utilisateur);
        when(avisRepo.findByUtilisateurId(1L)).thenThrow(NotFoundException.class);


        assertThrows(NotFoundException.class, () -> avisService.getAvisByUtilisateurId(1L));
    }

  @Test
    void testDeleteAllAvis_Success() {
        when(avisRepo.findAll()).thenReturn(List.of(avis));
        assertDoesNotThrow(() -> avisService.deleteAllAvis());
        verify(avisRepo, times(1)).deleteAll();
    }

  @Test
    void testDeleteAllAvis_Empty() {
        when(avisRepo.findAll()).thenThrow(NotFoundException.class);


        assertThrows(NotFoundException.class, () -> avisService.deleteAllAvis());
    }

  @Test
  void getPaginatedAvis() {
    Page<Avis> page = new PageImpl<>(List.of(avis));
    when(avisRepo.findAll(any(AvisFilter.class), any(PageRequest.class))).thenReturn(page);

    GetAvisPaged200Response response =
        avisService.getAllAvisPaged("product", null, "content", 0, 10);

    assertNotNull(response);
    assertEquals(1, response.getContent().size());
  }

  @Test
    void paginatedAvisNotFound() {
        when(avisRepo.findAll(any(AvisFilter.class),any(PageRequest.class))).thenReturn(Page.empty());

        GetAvisPaged200Response response = avisService.getAllAvisPaged("product", null, "new content", 0, 10);

        assertNotNull(response);
        assertEquals(0, response.getContent().size());
    }

}
