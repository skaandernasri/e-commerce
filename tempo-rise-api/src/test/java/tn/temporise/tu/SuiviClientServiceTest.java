package tn.temporise.tu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.application.mapper.SuiviClientMapper;
import tn.temporise.application.service.SuiviClientService;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.ProductRepo;
import tn.temporise.domain.port.SuiviClientRepo;
import tn.temporise.domain.port.UserRepo;
import tn.temporise.domain.port.UtilisateurAnonymeRepo;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SuiviClientServiceTest {

  @Mock
  private SuiviClientRepo suiviClientRepo;

  @Mock
  private ExceptionFactory exceptionFactory;

  @Mock
  private SuiviClientMapper suiviClientMapper;

  @Mock
  private ProductRepo productRepo;

  @Mock
  private UserRepo userRepo;

  @Mock
  private UtilisateurAnonymeRepo utilisateurAnonymeRepo;


  @InjectMocks
  private SuiviClientService suiviClientService;

  private SuiviClientRequest request;
  private UtilisateurModel utilisateur;
  private Produit produit;
  private UtilisateurAnonyme utilisateurAnonyme;
  private SuiviClient suiviClient;

  @BeforeEach
  void setUp() {
    utilisateur = UtilisateurModel.builder().id(1L).nom("user").email("email@gmail.com").build();

    produit = Produit.builder().id(1L).nom("product").prix(10.0).build();

    utilisateurAnonyme =
        UtilisateurAnonyme.builder().id(1L).sessionToken(UUID.randomUUID()).build();

    request = new SuiviClientRequest();
    request.setProduitId(1L);
    request.setTypeAction(SuiviClientRequest.TypeActionEnum.VIEW_PRODUCT);

    suiviClient = SuiviClient.builder().produit(produit).typeAction(TypeAction.VIEW_PRODUCT)
        .score(0.3).build();
  }

  @Test
  void saveSuiviClient_Success_RegisteredUser() {
    request.setUtilisateurId(1L);

    when(productRepo.findById(1L)).thenReturn(produit);
    when(userRepo.findById(1L)).thenReturn(utilisateur);
    when(suiviClientMapper.dtoToModel(request)).thenReturn(suiviClient);
    when(suiviClientRepo.save(any(SuiviClient.class))).thenReturn(suiviClient);

    Response response = suiviClientService.saveSuiviClient(request);

    assertNotNull(response);
    assertEquals("201", response.getCode());
    verify(suiviClientRepo, times(1)).save(any());
  }

  @Test
  void saveSuiviClient_Success_AnonymousUser() {
    request.setUtilisateurAnonymeUuid(utilisateurAnonyme.sessionToken());

    when(productRepo.findById(1L)).thenReturn(produit);
    when(utilisateurAnonymeRepo.findBySessionToken(utilisateurAnonyme.sessionToken()))
        .thenReturn(utilisateurAnonyme);
    when(suiviClientMapper.dtoToModel(request)).thenReturn(suiviClient);
    when(suiviClientRepo.save(any(SuiviClient.class))).thenReturn(suiviClient);

    Response response = suiviClientService.saveSuiviClient(request);

    assertNotNull(response);
    assertEquals("201", response.getCode());
    verify(suiviClientRepo, times(1)).save(any());
  }

  @Test
    void saveSuiviClient_NullRequest() {
        when(exceptionFactory.badRequest("badrequest.invalid_input"))
                .thenReturn(new BadRequestException("Invalid input", "4000"));

        assertThrows(BadRequestException.class, () ->
                suiviClientService.saveSuiviClient(null));
        verify(exceptionFactory, times(1)).badRequest("badrequest.invalid_input");
    }

  @Test
  void saveSuiviClient_ProductNotFound() {
    request.setUtilisateurId(1L);

    when(productRepo.findById(1L)).thenThrow(NotFoundException.class);

    assertThrows(NotFoundException.class, () -> suiviClientService.saveSuiviClient(request));
    verify(suiviClientRepo, never()).save(any());
  }

  @Test
  void saveSuiviClient_UserNotFound() {
    request.setUtilisateurId(1L);

    when(productRepo.findById(1L)).thenReturn(produit);
    when(suiviClientMapper.dtoToModel(request)).thenReturn(suiviClient);
    when(userRepo.findById(1L)).thenThrow(NotFoundException.class);
    assertThrows(NotFoundException.class, () -> suiviClientService.saveSuiviClient(request));
    verify(suiviClientRepo, never()).save(any());
  }

  @Test
  void saveSuiviClient_AnonymousUserNotFound() {
    request.setUtilisateurAnonymeUuid(utilisateurAnonyme.sessionToken());

    when(productRepo.findById(1L)).thenReturn(produit);
    when(suiviClientMapper.dtoToModel(request)).thenReturn(suiviClient);

    when(utilisateurAnonymeRepo.findBySessionToken(utilisateurAnonyme.sessionToken()))
        .thenThrow(NotFoundException.class);

    assertThrows(NotFoundException.class, () -> suiviClientService.saveSuiviClient(request));
    verify(suiviClientRepo, never()).save(any());
  }

  @Test
  void mergeAnonymousUserActions_Success() {
    request.setUtilisateurId(1L);
    request.setUtilisateurAnonymeUuid(utilisateurAnonyme.sessionToken());

    SuiviClient anonymousAction = SuiviClient.builder().produit(produit)
        .typeAction(TypeAction.VIEW_PRODUCT).utilisateurAnonyme(utilisateurAnonyme).build();

    when(userRepo.findById(1L)).thenReturn(utilisateur);
    when(utilisateurAnonymeRepo.findBySessionToken(utilisateurAnonyme.sessionToken()))
        .thenReturn(utilisateurAnonyme);
    when(suiviClientRepo.findByUtilisateurAnonymeId(1L)).thenReturn(List.of(anonymousAction));
    when(suiviClientRepo.findByUtilisateurId(1L)).thenReturn(List.of());
    when(suiviClientRepo.save(any())).thenReturn(anonymousAction);

    Response response = suiviClientService.mergeAnonymousUserActions(request);

    assertNotNull(response);
    assertEquals("201", response.getCode());
    verify(suiviClientRepo, times(1)).save(any());
  }

  @Test
    void mergeAnonymousUserActions_NullRequest() {
        when(exceptionFactory.badRequest("badrequest.invalid_input"))
                .thenReturn(new BadRequestException("Invalid input", "4000"));

        assertThrows(BadRequestException.class, () ->
                suiviClientService.mergeAnonymousUserActions(null));
        verify(exceptionFactory, times(1)).badRequest("badrequest.invalid_input");
    }

  @Test
  void mergeAnonymousUserActions_UserNotFound() {
    request.setUtilisateurId(1L);
    request.setUtilisateurAnonymeUuid(utilisateurAnonyme.sessionToken());

    when(userRepo.findById(1L)).thenThrow(NotFoundException.class);

    assertThrows(NotFoundException.class,
        () -> suiviClientService.mergeAnonymousUserActions(request));
  }

  @Test
  void mergeAnonymousUserActions_AnonymousUserNotFound() {
    request.setUtilisateurId(1L);
    request.setUtilisateurAnonymeUuid(utilisateurAnonyme.sessionToken());

    when(userRepo.findById(1L)).thenReturn(utilisateur);
    when(utilisateurAnonymeRepo.findBySessionToken(utilisateurAnonyme.sessionToken()))
        .thenThrow(NotFoundException.class);

    assertThrows(NotFoundException.class,
        () -> suiviClientService.mergeAnonymousUserActions(request));
  }
}
