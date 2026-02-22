package tn.temporise.tu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.application.mapper.UtilisateurAnonymeMapper;
import tn.temporise.application.service.UtilisateurAnonymeService;
import tn.temporise.domain.model.UtilisateurAnonyme;
import tn.temporise.domain.model.UtilisateurAnonymeRequest;
import tn.temporise.domain.model.UtilisateurAnonymeResponse;
import tn.temporise.domain.port.UtilisateurAnonymeRepo;
import tn.temporise.infrastructure.security.utils.CookiesUtil;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UtilisateurAnonymeServiceTest {

  @Mock
  private UtilisateurAnonymeRepo utilisateurAnonymeRepo;

  @Mock
  private ExceptionFactory exceptionFactory;

  @Mock
  private CookiesUtil cookiesUtil;
  @Mock
  private UtilisateurAnonymeMapper utilisateurAnonymeMapper;


  @InjectMocks
  private UtilisateurAnonymeService utilisateurAnonymeService;

  private UtilisateurAnonymeRequest request;
  private UtilisateurAnonyme utilisateurAnonyme;
  private UtilisateurAnonymeResponse response;
  private UUID sessionToken;
  private ResponseCookie responseCookie;

  @BeforeEach
  void setUp() {
    sessionToken = UUID.randomUUID();

    request = new UtilisateurAnonymeRequest();
    request.setSessionToken(sessionToken);
    utilisateurAnonyme = UtilisateurAnonyme.builder().sessionToken(sessionToken).build();

    response = new UtilisateurAnonymeResponse();
    response.setSessionToken(sessionToken);
    responseCookie = ResponseCookie.from("anonyme_session_token", sessionToken.toString()).build();
  }

  @Test
    void getOrCreateUtilisateurAnonyme_Success_ExistingUser() {
        when(utilisateurAnonymeRepo.findBySessionToken(sessionToken)).thenReturn(utilisateurAnonyme);
        when(utilisateurAnonymeMapper.modelToDto(utilisateurAnonyme)).thenReturn(response);
        when(cookiesUtil.createCookie(request.getSessionToken().toString(),"anonyme_session_token")).thenReturn(responseCookie);
      UtilisateurAnonymeResponse result = utilisateurAnonymeService.getOrCreateUtilisateurAnonyme(sessionToken);

        assertNotNull(result);
        assertEquals(sessionToken, result.getSessionToken());
        verify(utilisateurAnonymeRepo, never()).save(any());
    }

  @Test
    void getOrCreateUtilisateurAnonyme_Success_NewUser() {
        when(utilisateurAnonymeRepo.findBySessionToken(utilisateurAnonyme.sessionToken())).thenThrow(NotFoundException.class);
        when(utilisateurAnonymeRepo.save(any(UtilisateurAnonyme.class)))
              .thenReturn(utilisateurAnonyme);
        when(utilisateurAnonymeMapper.modelToDto(utilisateurAnonyme)).thenReturn(response);
      when(cookiesUtil.createCookie(request.getSessionToken().toString(),"anonyme_session_token")).thenReturn(responseCookie);

      UtilisateurAnonymeResponse result = utilisateurAnonymeService.getOrCreateUtilisateurAnonyme(sessionToken);

      assertNotNull(result);
      assertEquals(sessionToken, result.getSessionToken());
    }



  @Test
    void getUtilisateurAnonyme_Success() {
        when(utilisateurAnonymeRepo.findBySessionToken(sessionToken)).thenReturn(utilisateurAnonyme);
        when(utilisateurAnonymeMapper.modelToDto(utilisateurAnonyme)).thenReturn(response);

        UtilisateurAnonymeResponse result = utilisateurAnonymeService.getUtilisateurAnonyme(sessionToken);

        assertNotNull(result);
        assertEquals(sessionToken, result.getSessionToken());
    }
}
