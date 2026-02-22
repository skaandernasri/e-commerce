package tn.temporise.tu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.ConflictException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.service.CodePromoService;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.application.mapper.CodePromoMapper;
import tn.temporise.config.ZoneConfig;
import tn.temporise.domain.model.CodePromo;
import tn.temporise.domain.model.CodePromoRequest;
import tn.temporise.domain.model.CodePromoResponse;
import tn.temporise.domain.port.CodePromoRepo;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CodePromoServiceTest {

  @Mock
  private CodePromoRepo codePromoRepo;

  @Mock
  private CodePromoMapper codePromoMapper;
  @Mock
  private ZoneConfig zoneConfig;

  @Mock
  private ExceptionFactory exceptionFactory;

  @InjectMocks
  private CodePromoService codePromoService;

  private CodePromoRequest codePromoRequest;
  private CodePromo codePromo;
  private CodePromoResponse codePromoResponse;

  @BeforeEach
  void setUp() {
    codePromoRequest = new CodePromoRequest(null, "SUMMER20", 20.0);
    codePromo = CodePromo.builder().id(1L).code("SUMMER20").reduction(20.0)
        .dateExpiration(LocalDateTime.now(ZoneId.of("Europe/Paris")).plusSeconds(86400)).build();
    codePromoResponse = new CodePromoResponse();
    codePromoResponse.setDateExpiration(null);
    codePromoResponse.setId(codePromo.id());
    codePromoResponse.setReduction(codePromo.reduction());
    codePromoResponse.setCode(codePromo.code());
  }

  @Test
    void createCodePromo_Success() {
        when(codePromoMapper.dtoToModel(codePromoRequest)).thenReturn(codePromo);
        when(codePromoRepo.existsByCode("SUMMER20")).thenReturn(false);
        when(codePromoRepo.save(codePromo)).thenReturn(codePromo);
        when(codePromoMapper.modelToResponse(codePromo)).thenReturn(codePromoResponse);
        //when(zoneConfig.getTimezone()).thenReturn("Europe/Paris");
        CodePromoResponse result = codePromoService.createCodePromo(codePromoRequest);

        assertNotNull(result);
        assertEquals("SUMMER20", result.getCode());
        verify(codePromoRepo).save(codePromo);
    }

  @Test
    void createCodePromo_NullRequest() {
        when(exceptionFactory.badRequest("badrequest.invalid_request"))
                .thenThrow(new BadRequestException("Invalid request",""));

        assertThrows(BadRequestException.class, () -> codePromoService.createCodePromo(null));
    }

  @Test
    void createCodePromo_CodeAlreadyExists() {
        when(codePromoMapper.dtoToModel(codePromoRequest)).thenReturn(codePromo);
        when(codePromoRepo.existsByCode("SUMMER20")).thenReturn(true);
       // when(zoneConfig.getTimezone()).thenReturn("Europe/Paris");
        when(exceptionFactory.conflict("conflict.code_promo_exist"))
                .thenThrow(new ConflictException("Code already exists",""));

        assertThrows(ConflictException.class, () -> codePromoService.createCodePromo(codePromoRequest));
    }

  @Test
    void getCodePromoById_Success() {
        when(codePromoRepo.findById(1L)).thenReturn(codePromo);
        when(codePromoMapper.modelToResponse(codePromo)).thenReturn(codePromoResponse);

        CodePromoResponse result = codePromoService.getCodePromoById(1L);

        assertNotNull(result);
        assertEquals("SUMMER20", result.getCode());
    }

  @Test
    void getCodePromoById_NotFound() {
        when(codePromoRepo.findById(1L)).thenThrow(NotFoundException.class);


        assertThrows(NotFoundException.class, () -> codePromoService.getCodePromoById(1L));
    }

  @Test
  void getActiveCodePromos_Success() {
    CodePromoResponse codePromoResponse1 = new CodePromoResponse();
    codePromoResponse1.setDateExpiration(null);
    codePromoResponse1.setReduction(20.0);
    codePromoResponse1.setCode("ACTIVE20");
    CodePromo activeCodePromo = CodePromo.builder().code("ACTIVE20")
        .dateExpiration(LocalDateTime.now(ZoneOffset.UTC).plusSeconds(86400)).build();

    when(codePromoRepo.findByExpirationDateGreaterThan(any(LocalDateTime.class)))
        .thenReturn(List.of(activeCodePromo));
    when(codePromoMapper.modelToResponse(activeCodePromo)).thenReturn(codePromoResponse1);
    // when(zoneConfig.getTimezone()).thenReturn("Europe/Paris");

    List<CodePromoResponse> result = codePromoService.getActiveCodePromos();

    assertFalse(result.isEmpty());
    assertEquals("ACTIVE20", result.get(0).getCode());
  }

  @Test
  void updateCodePromo_Success() {
    CodePromo existing = CodePromo.builder().id(1L).code("OLDCODE")
        .dateExpiration(LocalDateTime.now(ZoneId.of("Europe/Paris")).plusSeconds(86400)).build();
    CodePromo updated = CodePromo.builder().id(1L).code("NEWCODE")
        .dateExpiration(LocalDateTime.now(ZoneId.of("Europe/Paris")).plusSeconds(86400)).build();
    CodePromoResponse codePromoResponse2 = new CodePromoResponse();
    codePromoResponse2.setDateExpiration(null);
    codePromoResponse2.setReduction(20.0);
    codePromoResponse2.setCode("NEWCODE");
    when(codePromoRepo.findById(1L)).thenReturn(existing);
    // when(codePromoRepo.findByCode("NEWCODE")).thenThrow(new NotFoundException("Not found",""));
    when(codePromoMapper.dtoToModel(codePromoRequest)).thenReturn(updated);
    when(codePromoRepo.save(any())).thenReturn(updated);
    when(codePromoMapper.modelToResponse(updated)).thenReturn(codePromoResponse2);

    CodePromoResponse result = codePromoService.updateCodePromo(1L, codePromoRequest);

    assertNotNull(result);
    assertEquals("NEWCODE", result.getCode());
  }
}
