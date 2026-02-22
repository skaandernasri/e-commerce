package tn.temporise.tu;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.service.NotifService;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.application.mapper.PromotionMapper;
import tn.temporise.application.service.PromotionService;
import tn.temporise.config.ZoneConfig;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.ProductRepo;
import tn.temporise.domain.port.PromotionRepo;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PromotionServiceTest {

  @Mock
  private PromotionRepo promotionRepo;
  @Mock
  private ProductRepo productRepo;
  @Mock
  private PromotionMapper promotionMapper;
  @Mock
  private ExceptionFactory exceptionFactory;
  @Mock
  private ZoneConfig zoneConfig;
  @Mock
  private NotifService notifService;
  @InjectMocks
  private PromotionService promotionService;

  private PromotionRequest promotionRequest;
  private Promotion promotion;
  private PromotionResponse promotionResponse;
  private LocalDateTime futureDate;
  private LocalDateTime pastDate;

  @BeforeEach
  void setUpTestData() {
    futureDate = LocalDateTime.now().plusDays(7);
    pastDate = LocalDateTime.now().minusDays(7);
    promotionRequest = new PromotionRequest();
    promotionRequest.setNom("Summer Sale");
    promotionRequest.setDescription("Discount on summer products");
    promotionRequest.setReduction(20.0);
    promotionRequest.setDateDebut(null);
    promotionRequest.setDateFin(null);
    promotionRequest.setProduitId(1L);
    promotion = new Promotion(1L, "Summer Sale", "Discount on summer products", 20.0,
        PromotionType.PERCENTAGE, pastDate, futureDate, null);
    promotionResponse = new PromotionResponse("Summer Sale", "Discount on summer products", 20.0,
        PromotionResponse.TypeEnum.PERCENTAGE, null, null);

  }

  @Test
  void testCreatePromotion_Success() {
    // Given
    Produit mockProduit = mock(Produit.class);
    when(mockProduit.promotions()).thenReturn(Collections.emptySet());
    // when(zoneConfig.getTimezone()).thenReturn("Europe/Paris");
    when(promotionMapper.dtoToModel(promotionRequest)).thenReturn(promotion);
    when(productRepo.findById(any())).thenReturn(mockProduit); // Return a mock Produit instead of
                                                               // any()
    when(promotionRepo.save(any(Promotion.class))).thenReturn(promotion);
    when(promotionMapper.modelToResponse(any(Promotion.class))).thenReturn(promotionResponse);

    // When
    PromotionResponse result = promotionService.createPromotion(promotionRequest);

    // Then
    assertNotNull(result);
    assertEquals(promotionResponse.getNom(), result.getNom());
  }

  @Test
    void testCreatePromotion_NullRequest() {
        when(exceptionFactory.badRequest("badrequest.invalid_request"))
                .thenThrow(new BadRequestException("",""));

        assertThrows(BadRequestException.class, () -> promotionService.createPromotion(null));
    }

  @Test
    void testGetPromotionById_NotFound() {
        when(promotionRepo.findById(1L)).thenReturn(null);
        when(exceptionFactory.notFound("notfound.promotion")).thenThrow(new NotFoundException("Promotion not found","4045"));
        assertThrows(NotFoundException.class, () -> promotionService.getPromotionById(1L));
    }



  @Test
    void testDeletePromotion_NotFound() {
        when(promotionRepo.findById(1L)).thenThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> promotionService.deletePromotion(1L));
    }

  @Test
    void testGetActivePromotions_Success() {
        when(promotionRepo.findActivePromotions(any(LocalDateTime.class))).thenReturn(List.of(promotion));
        when(promotionMapper.modelToResponse(promotion)).thenReturn(promotionResponse);
//        when(zoneConfig.getTimezone()).thenReturn("Europe/Paris");

        List<PromotionResponse> result = promotionService.getActivePromotions();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(promotionResponse.getNom(), result.get(0).getNom());
    }

  @Test
    void testGetActivePromotions_Empty() {
//        when(zoneConfig.getTimezone()).thenReturn("Europe/Paris");
        when(promotionRepo.findActivePromotions(any(LocalDateTime.class)))
                .thenThrow(new NotFoundException("There are no Promotions","4052"));
        assertThrows(NotFoundException.class, () -> promotionService.getActivePromotions());
    }

  @Test
    void testGetInactivePromotions_Success() {
        //when(zoneConfig.getTimezone()).thenReturn("Europe/Paris");
        when(promotionRepo.findInActivePromotions(any(LocalDateTime.class))).thenReturn(List.of(promotion));
        when(promotionMapper.modelToResponse(promotion)).thenReturn(promotionResponse);

        List<PromotionResponse> result = promotionService.getInactivePromotions();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(promotionResponse.getNom(), result.get(0).getNom());
    }

  @Test
    void testGetInactivePromotions_Empty() {
        //when(zoneConfig.getTimezone()).thenReturn("Europe/Paris");
        when(promotionRepo.findInActivePromotions(any(LocalDateTime.class))).thenThrow(new NotFoundException("There are no Promotions","4052"));
        assertThrows(NotFoundException.class, () -> promotionService.getInactivePromotions());

    }

  @Test
  void testValidateDate_InvalidRange() {

    LocalDateTime startDate = LocalDateTime.now();
    LocalDateTime endDate = LocalDateTime.now().minusDays(1); // end date before start date

    when(exceptionFactory.badRequest("badrequest.invalid_date_range"))
        .thenThrow(new BadRequestException("", ""));

    assertThrows(BadRequestException.class,
        () -> promotionService.validateDate(startDate, endDate));
  }
}
