package tn.temporise.tu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.ConflictException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.PanierMapper;
import tn.temporise.application.service.PanierService;
import tn.temporise.application.service.StockService;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.config.ZoneConfig;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.*;
import tn.temporise.infrastructure.persistence.entity.PanierVariantId;

import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PanierServiceTest {

  @Mock
  private PanierRepo panierRepo;
  @Mock
  private PanierMapper panierMapper;
  @Mock
  private ProductRepo productRepo;
  @Mock
  private ExceptionFactory exceptionFactory;
  @Mock
  private UserRepo userRepo;
  @Mock
  private PanierVariantRepo panierVariantRepo;
  @Mock
  private VariantRepo variantRepo;
  @Mock
  private ZoneConfig zoneConfig;

  @Mock
  private StockService stockService;

  @InjectMocks
  private PanierService panierService;

  private Panier panier;
  private UtilisateurModel user;
  private Produit product;
  private PanierVariant panierVariant;
  private CartItemRequest cartItem;
  private Variant variant;


  @BeforeEach
  void setUp() {
    user = UtilisateurModel.builder().id(1L).build();
    product = Produit.builder().id(1L).build();
    variant =
        Variant.builder().id(1L).produit(product).color("red").size("M").quantity(10L).build();
    panier = Panier.builder().id(1L).utilisateur(user).build();

    panierVariant = PanierVariant.builder().id(new PanierVariantId(1L, 1L)).panier(panier)
        .variant(variant).quantite(2L).build();

    cartItem = new CartItemRequest();
    cartItem.setId(1L);
    cartItem.setQuantite(2L);
    ReflectionTestUtils.setField(panierService, "reservedItemExpiration", Duration.ofMinutes(20));
  }

  @Test
    void getPanierById_Success() {
        when(panierRepo.findById(1L)).thenReturn(panier);
        when(panierVariantRepo.findByPanierId(1L)).thenReturn(List.of(panierVariant));
        when(variantRepo.findById(1L)).thenReturn(variant);
        when(panierMapper.modelToResponse(any())).thenReturn(new PanierResponse());

        PanierResponse response = panierService.getPanierById(1L);
        assertNotNull(response);
        verify(panierRepo).findById(1L);
    }

  @Test
    void getPanierById_ThrowsWhenIdIsNull() {
        when(exceptionFactory.badRequest(anyString())).thenReturn(new BadRequestException("Bad request", ""));
        assertThrows(BadRequestException.class, () -> panierService.getPanierById(null));
    }

  @Test
    void getAllPaniers_Success() {
        when(panierRepo.findAll()).thenReturn(List.of(panier));
        when(panierMapper.modelToResponse(any())).thenReturn(new PanierResponse());

        List<PanierResponse> response = panierService.getAllPaniers();
        assertFalse(response.isEmpty());
        verify(panierRepo).findAll();
    }

  @Test
    void deletePanier_Success() {
        when(panierRepo.findById(1L)).thenReturn(panier);
        panierService.deletePanier(1L);
        verify(panierRepo).deleteById(1L);
    }

  @Test
  void deleteAllPaniers_Success() {
    panierService.deleteAllPaniers();
    verify(panierRepo).deleteAll();
  }

  @Test
    void getOrCreateCart_ExistingCart() {
        when(panierRepo.findByUtilisateurId(1L)).thenReturn(panier);
        when(panierVariantRepo.findByPanierId(1L)).thenReturn(List.of(panierVariant));
        when(variantRepo.findById(1L)).thenReturn(variant);
        when(panierMapper.modelToResponse(any())).thenReturn(new PanierResponse());

        PanierResponse response = panierService.getOrCreateCart(1L);
        assertNotNull(response);
        verify(panierRepo).findByUtilisateurId(1L);
    }

  @Test
    void getOrCreateCart_NewCart() {
        when(panierRepo.findByUtilisateurId(1L)).thenThrow(new NotFoundException("Not found", ""));
        when(userRepo.findById(1L)).thenReturn(user);
        when(panierRepo.save(any())).thenReturn(panier);
        when(panierMapper.modelToResponse(any())).thenReturn(new PanierResponse());

        PanierResponse response = panierService.getOrCreateCart(1L);
        assertNotNull(response);
        verify(panierRepo).save(any());
    }

  @Test
    void addItem_NewItem() {
        when(userRepo.findById(1L)).thenReturn(user);
        when(variantRepo.findByIdForUpdate(1L)).thenReturn(variant);
        when(panierRepo.findByUtilisateurId(1L)).thenReturn(panier);
        when(panierVariantRepo.findByPanierIdAndVariantId(1L, 1L)).thenThrow(new NotFoundException("Not found", ""));
        when(panierMapper.modelToResponse(any())).thenReturn(new PanierResponse());
        PanierResponse response = panierService.addItem(1L, 1L, cartItem);
        assertNotNull(response);
        verify(panierVariantRepo).save(any());
    }

  @Test
    void addItem_ExistingItem() {
        when(userRepo.findById(1L)).thenReturn(user);
        when(variantRepo.findByIdForUpdate(1L)).thenReturn(variant);
        when(panierRepo.findByUtilisateurId(1L)).thenReturn(panier);
        when(panierVariantRepo.findByPanierIdAndVariantId(1L, 1L)).thenReturn(panierVariant);
        when(panierMapper.modelToResponse(any())).thenReturn(new PanierResponse());

        PanierResponse response = panierService.addItem(1L, 1L, cartItem);
        assertNotNull(response);
        verify(panierVariantRepo).save(any());
    }

  @Test
  void addItem_ThrowsWhenQuantityInvalid() {
    cartItem.setQuantite(0L);
    BadRequestException badRequestException =
        new BadRequestException("badrequest.invalid_input", "");
    when(exceptionFactory.badRequest("badrequest.invalid_input")).thenReturn(badRequestException);
    assertThrows(BadRequestException.class, () -> panierService.addItem(1L, 1L, cartItem));
  }

  @Test
  void addItem_ThrowsWhenInsufficientStock() {
    variant = variant.toBuilder().quantity(0L).build();

    when(userRepo.findById(1L)).thenReturn(user);
    when(variantRepo.findByIdForUpdate(1L)).thenReturn(variant);
    when(panierRepo.findByUtilisateurId(1L)).thenReturn(panier);
    ConflictException conflictException = new ConflictException("conflict.insufficient_stock", "");
    when(exceptionFactory.conflict("conflict.insufficient_stock")).thenReturn(conflictException);
    doThrow(ConflictException.class).when(stockService).verifyStockAvailability(variant,
        cartItem.getQuantite());
    assertThrows(ConflictException.class, () -> panierService.addItem(1L, 1L, cartItem));
  }

  @Test
    void updateItemQuantity_Success() {
        when(userRepo.findById(1L)).thenReturn(user);
        when(variantRepo.findById(1L)).thenReturn(variant);
        when(panierRepo.findByUtilisateurId(1L)).thenReturn(panier);
        when(panierVariantRepo.findByPanierIdAndVariantId(1L, 1L)).thenReturn(panierVariant);
        when(panierMapper.modelToResponse(any())).thenReturn(new PanierResponse());
//        when(zoneConfig.getTimezone()).thenReturn("Europe/Paris");

        PanierResponse response = panierService.updateItemQuantity(1L, 1L, cartItem);
        assertNotNull(response);
        verify(panierVariantRepo).save(any());
    }

  @Test
    void removeItem_Success() {
        when(userRepo.findById(1L)).thenReturn(user);
        when(panierRepo.findByUtilisateurId(1L)).thenReturn(panier);
        when(panierVariantRepo.findByPanierIdAndVariantId(1L, 1L)).thenReturn(panierVariant);

        panierService.removeItem(1L, 1L);
        verify(panierVariantRepo).deleteById(any());
    }

  @Test
    void removeAllItemsFromPanier_Success() {
        when(userRepo.findById(1L)).thenReturn(user);
        when(panierRepo.findByUtilisateurId(1L)).thenReturn(panier);

        panierService.removeAllItemsFromPanier(1L, 1L);
        verify(panierVariantRepo).deleteByPanierId(1L);
    }

  @Test
    void mergeCart_Success() {
        when(userRepo.findById(1L)).thenReturn(user);
        when(panierRepo.findByUtilisateurId(1L)).thenReturn(panier);
        when(variantRepo.findById(1L)).thenReturn(variant);
        when(panierVariantRepo.findByPanierIdAndVariantId(1L, 1L)).thenThrow(new NotFoundException("Not found", ""));
        when(panierMapper.modelToResponse(any())).thenReturn(new PanierResponse());

        List<CartItemRequest> items = List.of(cartItem);
        PanierResponse response = panierService.mergeCart(1L, items);
        assertNotNull(response);
        verify(panierVariantRepo).save(any());
    }

}
