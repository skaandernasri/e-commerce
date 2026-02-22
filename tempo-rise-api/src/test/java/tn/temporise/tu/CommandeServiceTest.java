package tn.temporise.tu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.service.CommandeService;
import tn.temporise.application.service.NotifService;
import tn.temporise.application.service.PanierService;
import tn.temporise.application.service.PaymentService;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.application.mapper.CommandeMapper;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommandeServiceTest {

  @Mock
  private CommandeRepo commandeRepo;
  @Mock
  private LigneCommandeRepo ligneCommandeRepo;
  @Mock
  private UserRepo utilisateurRepo;
  @Mock
  private CommandeMapper commandeMapper;
  @Mock
  private ExceptionFactory exceptionFactory;
  @Mock
  private PanierVariantRepo panierVariantRepo;
  @Mock
  private PanierRepo panierRepo;
  @Mock
  private CodePromoRepo codePromoRepo;
  @Mock
  private PaymentService paymentService;
  @Mock
  private NotifService notifService;
  @Mock
  private FactureRepo factureRepo;
  @Mock
  private EmailInterface emailInterface;
  @Mock
  private PanierService panierService;

  @Mock
  private VariantRepo variantRepo;

  @Mock
  private PaiementRepo paiementRepo;



  @InjectMocks
  private CommandeService commandeService;

  private CommandeRequest commandeRequest;
  private Commande commande;
  private CommandeResponse commandeResponse;
  private UtilisateurModel utilisateur;
  private Panier panier;
  private Produit produit;
  private CodePromo codePromo;
  private Variant variant;
  private PaiementResponse paiementResponse;
  private Paiement paiement;

  @BeforeEach
  void setUpTestData() {
    utilisateur =
        UtilisateurModel.builder().id(1L).email("test@example.com").isverified(true).build();
    panier = new Panier(1L, null, utilisateur);
    produit =
        Produit.builder().id(1L).nom("Test Product").description("Description").prix(100.0).build();
    variant =
        Variant.builder().id(1L).produit(produit).color("Red").size("M").quantity(10L).build();
    codePromo =
        new CodePromo(1L, "SUMMER20", 20.0, LocalDateTime.now(ZoneId.of("UTC")).plusSeconds(86400));
    commandeRequest = new CommandeRequest();
    commandeRequest.setUtilisateurId(1L);
    commandeRequest.setToken("tok_test");
    commandeRequest.setShipping(20.0);
    commandeRequest.setTotal(120.0);
    // commandeRequest.setModePaiement("CARTE_BANCAIRE");
    // commandeRequest.setStatut("EN_COURS");
    commandeRequest.setCodePromo(codePromo.code());
    LigneCommande ligneCommande =
        LigneCommande.builder().id(1L).variant(variant).quantite(1L).prixTotal(100.0).build();
    commande = Commande.builder().id(1L).user(utilisateur).modePaiement(ModePaiement.A_LIVRAISON)
        .statut(StatutCommande.EN_COURS).lignesCommande(Collections.singletonList(ligneCommande))
        .total(120.0).build();

    commandeResponse = new CommandeResponse();
    commandeResponse.setId(1L);
    CommandeResponseUtilisateur utilisateurResponse = new CommandeResponseUtilisateur();
    utilisateurResponse.setId(1L);
    commandeResponse.setUtilisateur(utilisateurResponse);
    commandeResponse.setModePaiement("CARTE_BANCAIRE");
    commandeResponse.setStatut(StatusCommandeEnum.EN_COURS);

    paiementResponse = new PaiementResponse();
    paiementResponse.setPayUrl(URI.create("https://payment.test"));
    paiementResponse.setPaymentRef("REF123");

    paiement = Paiement.builder().commande(commande).paiementRef("REF123").build();

  }

  // @Test
  // void testCreateCommande_Success() {
  // // Arrange
  // commandeRequest.setToken("tok_test");
  //
  // when(utilisateurRepo.findById(1L)).thenReturn(utilisateur);
  // // when(codePromoRepo.findByCode("SUMMER20")).thenReturn(codePromo);
  // when(commandeMapper.dtoToModel(any(CommandeRequest.class))).thenReturn(commande);
  // when(commandeRepo.save(any(Commande.class))).thenReturn(commande);
  //
  // // Act
  // PaiementResponse result = commandeService.createCommande(commandeRequest);
  //
  // // Assert
  // assertNotNull(result);
  // assertEquals("1", result.getPaymentRef());
  // verify(commandeRepo, times(1)).save(any(Commande.class));
  // }


  @Test
    void testCreateCommande_NullRequest() {
        when(exceptionFactory.badRequest("badrequest.invalid_request"))
                .thenThrow(new BadRequestException("",""));

        assertThrows(BadRequestException.class, () -> commandeService.createCommande(null));
    }

  @Test
    void testGetCommandeById_Success() {
        when(commandeRepo.findById(1L)).thenReturn(commande);
        when(commandeMapper.modelToDto(any(Commande.class))).thenReturn(commandeResponse);

        CommandeResponse result = commandeService.getCommandeById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

  @Test
    void testGetCommandeById_NotFound() {
        when(commandeRepo.findById(1L)).thenThrow(NotFoundException.class);


        assertThrows(NotFoundException.class, () -> commandeService.getCommandeById(1L));
    }

  @Test
    void testGetAllCommandes_Success() {
        when(commandeRepo.findAll()).thenReturn(List.of(commande));
        when(commandeMapper.modelToDto(any(Commande.class))).thenReturn(commandeResponse);
        List<CommandeResponse> result = commandeService.getAllCommandes();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

  @Test
    void testGetCommandesByUtilisateurId_Success() {
        when(utilisateurRepo.findById(1L)).thenReturn(utilisateur);
        when(commandeRepo.findByUserId(1L)).thenReturn(List.of(commande));
        when(commandeMapper.modelToDto(any(Commande.class))).thenReturn(commandeResponse);

        List<CommandeResponse> result = commandeService.getCommandesByUtilisateurId(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

  @Test
  void testUpdateStatutCommande_Success() {

    Commande updatedCommande = Commande.builder().id(1L).user(utilisateur)
        .lignesCommande(List.of(
            LigneCommande.builder().id(1L).variant(variant).quantite(2L).prixTotal(200.0).build()))
        .modePaiement(ModePaiement.CARTE_BANCAIRE).statut(StatutCommande.LIVREE).build();
    UpdateCommandeStatusRequest updateCommandeRequest = new UpdateCommandeStatusRequest();
    updateCommandeRequest.setStatus(StatusCommandeEnum.LIVREE);
    CommandeResponseUtilisateur utilisateurResponse = new CommandeResponseUtilisateur();
    utilisateurResponse.setId(1L);

    CommandeResponse updatedResponse = new CommandeResponse();
    updatedResponse.setId(1L);
    updatedResponse.setUtilisateur(utilisateurResponse);
    updatedResponse.setModePaiement("CARTE_BANCAIRE");
    updatedResponse.setStatut(StatusCommandeEnum.LIVREE);

    // Mock the repository methods
    when(commandeRepo.findById(1L)).thenReturn(commande);
    when(commandeRepo.save(any(Commande.class))).thenReturn(updatedCommande);
    when(commandeMapper.modelToDto(updatedCommande)).thenReturn(updatedResponse);

    // Call the method to be tested
    CommandeResponse result = commandeService.updateStatusCommande(1L, updateCommandeRequest);

    // Assertions
    assertNotNull(result);
    assertEquals(StatusCommandeEnum.LIVREE, result.getStatut());
  }



}
