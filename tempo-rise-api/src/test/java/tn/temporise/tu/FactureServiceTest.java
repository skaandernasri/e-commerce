package tn.temporise.tu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.FactureMapper;
import tn.temporise.application.service.FactureService;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.CommandeRepo;
import tn.temporise.domain.port.FactureRepo;
import tn.temporise.domain.port.UserRepo;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FactureServiceTest {

  @Mock
  private FactureRepo factureRepo;

  @Mock
  private FactureMapper factureMapper;

  @Mock
  private ExceptionFactory exceptionFactory;

  @Mock
  private UserRepo userRepo;

  @Mock
  private CommandeRepo commandeRepo;

  @InjectMocks
  private FactureService factureService;

  private FactureRequest factureRequest;
  private Facture facture;
  private FactureResponse factureResponse;
  private Commande commande;
  private UtilisateurModel user;

  @BeforeEach
  void setUp() {
    user = UtilisateurModel.builder().id(1L).email("test@example.com").build();
    commande = Commande.builder().id(1L).user(user).total(100.0).build();
    factureRequest = new FactureRequest();
    factureRequest.setCommandeId(1L);
    factureRequest.setTotal(commande.total());
    facture = Facture.builder().id(1L).dateEmission(LocalDateTime.now()).total(100.0)
        .commande(commande).build();

    factureResponse = new FactureResponse();
    factureResponse.setId(1L);
  }

  @Test
    void getAllFactures_Success() {
        when(factureRepo.findAll()).thenReturn(List.of(facture));
        when(factureMapper.modelToDto(facture)).thenReturn(factureResponse);

        List<FactureResponse> result = factureService.getAllFactures();

        assertFalse(result.isEmpty());
        assertEquals(1L, result.get(0).getId());
        verify(factureRepo).findAll();
    }

  @Test
    void saveFacture_Success() {
        when(factureMapper.dtoToModel(factureRequest)).thenReturn(facture);
        when(factureRepo.save(facture)).thenReturn(facture);
        when(factureMapper.modelToDto(facture)).thenReturn(factureResponse);

        FactureResponse result = factureService.saveFacture(factureRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(factureRepo).save(facture);
    }

  @Test
    void saveFacture_NullRequest() {
        when(exceptionFactory.badRequest("badrequest.invalid_request"))
                .thenThrow(new BadRequestException("Invalid request", ""));

        assertThrows(BadRequestException.class, () -> factureService.saveFacture(null));
    }

  @Test
  void saveFacture_MissingCommandeId() {
    FactureRequest invalidRequest = new FactureRequest();
    invalidRequest.setCommandeId(null);
    invalidRequest.setTotal(100.0);
    when(exceptionFactory.badRequest("badrequest.invalid_request"))
        .thenThrow(new BadRequestException("Commande ID is required", ""));

    assertThrows(BadRequestException.class, () -> factureService.saveFacture(invalidRequest));
  }

  @Test
    void getFactureById_Success() {
        when(factureRepo.findById(1L)).thenReturn(facture);
        when(factureMapper.modelToDto(facture)).thenReturn(factureResponse);

        FactureResponse result = factureService.getFactureById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

  @Test
    void getFactureById_NotFound() {
        when(factureRepo.findById(1L)).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> factureService.getFactureById(1L));
    }

  @Test
    void getFacturesByUserId_Success() {
        when(userRepo.findById(1L)).thenReturn(user);
        when(factureRepo.findByUtilisateurId(1L)).thenReturn(List.of(facture));
        when(factureMapper.modelToDto(facture)).thenReturn(factureResponse);

        List<FactureResponse> result = factureService.getFacturesByUserId(1L);

        assertFalse(result.isEmpty());
        assertEquals(1L, result.get(0).getId());
    }

  @Test
    void getFacturesByUserId_UserNotFound() {
        when(userRepo.findById(1L)).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> factureService.getFacturesByUserId(1L));
    }

  // @Test
  // void getFacturesByCommandeId_Success() {
  // when(commandeRepo.findById(1L)).thenReturn(commande);
  // when(factureRepo.findByCommandeId(1L)).thenReturn(List.of(facture));
  // when(factureMapper.modelToDto(facture)).thenReturn(factureResponse);
  //
  // List<FactureResponse> result = factureService.getFacturesByCommandeId(1L);
  //
  // assertFalse(result.isEmpty());
  // assertEquals(1L, result.get(0).getId());
  // }

  @Test
    void getFacturesByCommandeId_CommandeNotFound() {
        when(commandeRepo.findById(1L)).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> factureService.getFacturesByCommandeId(1L));
    }

  @Test
    void deleteFacture_Success() {
        when(factureRepo.findById(1L)).thenReturn(facture);
        doNothing().when(factureRepo).deleteById(1L);

        assertDoesNotThrow(() -> factureService.deleteFacture(1L));
        verify(factureRepo).deleteById(1L);
    }

  @Test
    void deleteFacture_NotFound() {
        when(factureRepo.findById(1L)).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> factureService.deleteFacture(1L));
    }

  @Test
  void deleteAllFactures_Success() {
    doNothing().when(factureRepo).deleteAll();

    assertDoesNotThrow(() -> factureService.deleteAllFactures());
    verify(factureRepo).deleteAll();
  }
}
