package tn.temporise.tu;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.service.UtilisateurAnonymeService;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.application.mapper.AdresseMapper;
import tn.temporise.application.service.AdresseService;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.AdresseRepo;
import tn.temporise.domain.port.CommandeRepo;
import tn.temporise.domain.port.UserRepo;
import tn.temporise.infrastructure.security.utils.CookiesUtil;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class AdresseServiceTest {

  @Mock
  private AdresseRepo adresseRepo;

  @Mock
  private AdresseMapper adresseMapper;

  @Mock
  private ExceptionFactory exceptionFactory;

  @Mock
  private CommandeRepo commandeRepo;

  @Mock
  private UserRepo userRepo;

  @Mock
  private CookiesUtil cookiesUtil;
  @Mock
  private UtilisateurAnonymeService utilisateurAnonymeService;

  @InjectMocks
  private AdresseService adresseService;

  private AdresseRequest adresseRequest;
  private Adresse adresse;
  private AdresseResponse adresseResponse;
  private UtilisateurModel user;

  @BeforeEach
  void setUp() {
    adresseRequest = new AdresseRequest();
    adresseRequest.setCodePostal("1000");
    adresseRequest.setVille("Tunis");
    adresseRequest.setPays("Tunisia");
    adresseRequest.setType("LIVRAISON");
    adresseRequest.setUtilisateurId(1L);
    user = UtilisateurModel.builder().id(1L).email("test@example").nom("test").build();
    adresse = Adresse.builder().id(1L).codePostal("1000").ville("Tunis").pays("Tunisia")
        .type(TypeAdresse.LIVRAISON).utilisateur(user).build();
    AdresseResponseUtilisateur utilisateurResponse = new AdresseResponseUtilisateur();
    utilisateurResponse.setId(1L);
    utilisateurResponse.setEmail("test@example");
    utilisateurResponse.setNom("test");
    adresseResponse = new AdresseResponse();
    adresseResponse.setId(1L);
    adresseResponse.setCodePostal("1000");
    adresseResponse.setVille("Tunis");
    adresseResponse.setPays("Tunisia");
    adresseResponse.setType("LIVRAISON");
    adresseResponse.setUtilisateur(utilisateurResponse);
  }

  @Test
    void testCreateAdresse_Success() {
        when(adresseMapper.dtoToModel(adresseRequest)).thenReturn(adresse);
        when(adresseRepo.save(adresse)).thenReturn(adresse);
        when(adresseMapper.modelToDto(adresse)).thenReturn(adresseResponse);
        when(userRepo.findAllTypeOfUserById(adresseRequest.getUtilisateurId())).thenReturn(user);
        AdresseResponse result = adresseService.createAdresse(adresseRequest);

        assertNotNull(result);
        assertEquals(adresseResponse.getId(), result.getId());
        verify(adresseRepo, times(1)).save(adresse);
    }

  @Test
    void testCreateAdresse_NullRequest() {
        when(exceptionFactory.badRequest("badrequest.invalid_input"))
                .thenReturn(new BadRequestException("Invalid input", "4000"));

        assertThrows(BadRequestException.class, () -> adresseService.createAdresse(null));
        verify(exceptionFactory, times(1)).badRequest("badrequest.invalid_input");
    }

  @Test
  void testCreateAdresse_MissingRequiredFields() {
    AdresseRequest invalidRequest = new AdresseRequest();
    when(exceptionFactory.badRequest("badrequest.invalid_input"))
        .thenReturn(new BadRequestException("Invalid input", "4000"));

    assertThrows(BadRequestException.class, () -> adresseService.createAdresse(invalidRequest));
  }

  @Test
  void testCreateAdresse_InvalidType() {
    adresseRequest.setType("INVALID");
    when(exceptionFactory.badRequest("badrequest.invalid_input"))
        .thenReturn(new BadRequestException("Invalid input", "4000"));

    assertThrows(BadRequestException.class, () -> adresseService.createAdresse(adresseRequest));
  }

  @Test
    void testGetAdresseById_Success() {
        when(adresseRepo.findById(1L)).thenReturn(adresse);
        when(adresseMapper.modelToDto(adresse)).thenReturn(adresseResponse);

        AdresseResponse result = adresseService.getAdresseById(1L);

        assertNotNull(result);
        assertEquals(adresseResponse.getId(), result.getId());
    }

  @Test
    void testGetAdresseById_NullId() {
        when(exceptionFactory.badRequest("badrequest.invalid_id"))
                .thenReturn(new BadRequestException("Invalid ID", "4000"));

        assertThrows(BadRequestException.class, () -> adresseService.getAdresseById(null));
        verify(exceptionFactory, times(1)).badRequest("badrequest.invalid_id");
    }

  @Test
    void testGetAdresseById_NotFound() {
        when(adresseRepo.findById(1L)).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> adresseService.getAdresseById(1L));
    }

  @Test
    void testGetAllAdresses_Success() {
        when(adresseRepo.findAll()).thenReturn(List.of(adresse));
        when(adresseMapper.modelToDto(adresse)).thenReturn(adresseResponse);

        List<AdresseResponse> result = adresseService.getAllAdresses();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

  @Test
    void testGetAllAdresses_Empty() {
        when(adresseRepo.findAll()).thenReturn(Collections.emptyList());

        assertEquals(0, adresseService.getAllAdresses().size());
    }

  @Test
    void testGetAdressesByType_Success() {
        when(adresseRepo.findByType(TypeAdresse.LIVRAISON)).thenReturn(List.of(adresse));
        when(adresseMapper.modelToDto(adresse)).thenReturn(adresseResponse);

        List<AdresseResponse> result = adresseService.getAdressesByType("LIVRAISON");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

  @Test
    void testGetAdressesByType_NullType() {
        when(exceptionFactory.badRequest("badrequest.invalid_input"))
                .thenReturn(new BadRequestException("Invalid input", "4000"));

        assertThrows(BadRequestException.class, () -> adresseService.getAdressesByType(null));
    }

  @Test
    void testGetAdressesByUtilisateurId_Success() {
        when(adresseRepo.findByUtilisateurId(1L)).thenReturn(List.of(adresse));
        when(adresseMapper.modelToDto(adresse)).thenReturn(adresseResponse);

        List<AdresseResponse> result = adresseService.getAdressesByUtilisateurId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

  @Test
  void testGetAdressesByUtilisateurId_NullId() {
    UUID uuid = UUID.randomUUID();
    UtilisateurAnonymeResponse response = new UtilisateurAnonymeResponse();
    response.setId(1L);
    when(cookiesUtil.getUUIDCookieValue("anonyme_session_token")).thenReturn(uuid);
    when(utilisateurAnonymeService.getOrCreateUtilisateurAnonyme(uuid)).thenReturn(response);
    when(adresseRepo.findByUtilisateurId(1L)).thenReturn(List.of(adresse));
    when(adresseMapper.modelToDto(adresse)).thenReturn(adresseResponse);

    List<AdresseResponse> result = adresseService.getAdressesByUtilisateurId(null);

    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
    void testUpdateAdresse_Success() {
        when(adresseRepo.findById(1L)).thenReturn(adresse);
        when(adresseRepo.save(adresse)).thenReturn(adresse);
        when(adresseMapper.modelToDto(adresse)).thenReturn(adresseResponse);

        AdresseResponse result = adresseService.updateAdresse(1L, adresseRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

  @Test
    void testUpdateAdresse_NullId() {
        when(exceptionFactory.badRequest("badrequest.invalid_id"))
                .thenReturn(new BadRequestException("Invalid ID", "4000"));

        assertThrows(BadRequestException.class, () -> adresseService.updateAdresse(null, adresseRequest));
    }

  @Test
    void testUpdateAdresse_NotFound() {
        when(adresseRepo.findById(1L)).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> adresseService.updateAdresse(1L, adresseRequest));
    }

  @Test
    void testDeleteAdresse_Success() {
        when(adresseRepo.findById(1L)).thenReturn(adresse);
        assertDoesNotThrow(() -> adresseService.deleteAdresse(1L));
        verify(adresseRepo, times(1)).deleteById(1L);
    }

  @Test
    void testDeleteAdresse_NullId() {
        when(exceptionFactory.badRequest("badrequest.invalid_id"))
                .thenReturn(new BadRequestException("Invalid ID", "4000"));

        assertThrows(BadRequestException.class, () -> adresseService.deleteAdresse(null));
    }

  @Test
    void testDeleteAllAdresses_Success() {
        when(adresseRepo.findAll()).thenReturn(List.of(adresse));
        assertDoesNotThrow(() -> adresseService.deleteAllAdresses());
        verify(adresseRepo, times(1)).deleteAll();
    }

  @Test
    void testDeleteAllAdresses_Empty() {
        when(adresseRepo.findAll()).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> adresseService.deleteAllAdresses());
    }
}
