package tn.temporise.tu;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.temporise.application.exception.auth.UnauthorizedException;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.AuthMapper;
import tn.temporise.application.service.CustomUserDetailsService;
import tn.temporise.application.service.TokenService;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.Authentification;
import tn.temporise.domain.model.CustomUserDetails;
import tn.temporise.domain.model.TokenResponse;
import tn.temporise.domain.model.TypeAuthentification;
import tn.temporise.domain.port.AuthRepo;
import tn.temporise.infrastructure.persistence.entity.AuthentificationEntity;
import tn.temporise.infrastructure.security.utils.JwtUtil;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

  @Mock
  private CustomUserDetailsService userDetailsService;

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private AuthRepo authRepo;

  @Mock
  private AuthMapper authMapper;

  @Mock
  private ExceptionFactory exceptionFactory;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @InjectMocks
  private TokenService tokenService;

  private final String testEmail = "test@example.com";
  private final String testProviderId = "provider123";
  private final String testToken = "testToken";
  private final String testRefreshToken = "refreshToken";
  private CustomUserDetails userDetails;

  @BeforeEach
  void setUp() {
    userDetails = CustomUserDetails.builder().email(testEmail).providerId(testProviderId).build();
  }

  @Test
    void refreshToken_Success() throws Exception {
        // Arrange
        when(jwtUtil.getJwtFromCookies(request)).thenReturn(testToken);
        when(jwtUtil.extractEmail(testToken)).thenReturn(testEmail);
        when(jwtUtil.extractProviderId(testToken)).thenReturn(testProviderId);
        when(userDetailsService.loadUserByUsername(testEmail + ";" + testProviderId)).thenReturn(userDetails);
        Authentification auth= Authentification.builder()
                .token(testRefreshToken)
                .type(TypeAuthentification.EMAIL)
                .providerId(testProviderId)
                .password(null)
                .build();
        when(authRepo.findByUserEmailAndProviderId(testEmail, testProviderId)).thenReturn(auth);
        when(jwtUtil.validateToken(testRefreshToken, userDetails)).thenReturn(true);
        when(jwtUtil.generateAccessToken(userDetails)).thenReturn("newAccessToken");
        when(jwtUtil.generateRefreshToken(userDetails)).thenReturn("newRefreshToken");

        AuthentificationEntity authEntity = new AuthentificationEntity();
        when(authMapper.modelToEntity(auth)).thenReturn(authEntity);
        when(authRepo.save(any())).thenReturn(auth);

        // Act
        TokenResponse response = tokenService.refreshToken(request);

        // Assert
        assertNotNull(response);
        assertEquals("newAccessToken", response.getToken());
        assertEquals("newRefreshToken", response.getRefreshToken());
        verify(jwtUtil).setJwtCookie(this.response, "newAccessToken");
    }

  @Test
    void refreshToken_MissingToken_ThrowsNotFoundException() {
        // Arrange
        when(jwtUtil.getJwtFromCookies(request)).thenReturn(null);
        when(exceptionFactory.notFound("notfound.token")).thenReturn(new NotFoundException("Token not found",""));

        // Act & Assert
        assertThrows(NotFoundException.class, () -> tokenService.refreshToken(request));
    }

  @Test
    void refreshToken_InvalidToken_ThrowsUnauthorizedException() {
        // Arrange
        when(jwtUtil.getJwtFromCookies(request)).thenReturn(testToken);
        when(jwtUtil.extractEmail(testToken)).thenReturn(null);
        when(exceptionFactory.unauthorized("unauthorized.invalid_token"))
                .thenReturn(new UnauthorizedException("Invalid token",""));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> tokenService.refreshToken(request));
    }

  @Test
    void refreshToken_InvalidRefreshToken_ThrowsUnauthorizedException() throws Exception {
        // Arrange
        when(jwtUtil.getJwtFromCookies(request)).thenReturn(testToken);
        when(jwtUtil.extractEmail(testToken)).thenReturn(testEmail);
        when(jwtUtil.extractProviderId(testToken)).thenReturn(testProviderId);
        when(userDetailsService.loadUserByUsername(testEmail + ";" + testProviderId)).thenReturn(userDetails);
        Authentification auth= Authentification.builder()
                .token(testRefreshToken)
                .type(TypeAuthentification.EMAIL)
                .providerId(testProviderId)
                .password(null)
                .build();
        when(authRepo.findByUserEmailAndProviderId(testEmail, testProviderId)).thenReturn(auth);
        when(jwtUtil.validateToken(testRefreshToken, userDetails)).thenReturn(false);
        when(exceptionFactory.unauthorized("unauthorized.invalid_token"))
                .thenReturn(new UnauthorizedException("Invalid token",""));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> tokenService.refreshToken(request));
    }

  @Test
  void saveToken_Success() {
    // Arrange
    Authentification auth = new Authentification(null, "oldToken", null, null);
    AuthentificationEntity authEntity = new AuthentificationEntity();
    Authentification savedAuth = new Authentification(null, testToken, null, null);

    when(authRepo.findByUserEmailAndProviderId(testEmail, testProviderId)).thenReturn(auth);
    when(authMapper.modelToEntity(auth)).thenReturn(authEntity);
    when(authRepo.save(any())).thenReturn(savedAuth);

    // Act
    tokenService.saveToken(testEmail, testToken, testProviderId);

    // Assert
    verify(authRepo).save(any());
    verify(authMapper).entityToModel(authEntity);
  }

  @Test
    void saveToken_MissingParameters_ThrowsBadRequestException() {
        // Arrange
        when(exceptionFactory.badRequest("badrequest.missing_token"))
                .thenReturn(new BadRequestException("Missing token parameters",""));

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> tokenService.saveToken(null, testToken, testProviderId));
        assertThrows(BadRequestException.class,
                () -> tokenService.saveToken(testEmail, null, testProviderId));
        assertThrows(BadRequestException.class,
                () -> tokenService.saveToken(testEmail, testToken, null));
    }

  @Test
  void removeToken_Success() {
    // Arrange
    Authentification auth = new Authentification(null, testToken, null, null);
    AuthentificationEntity authEntity = new AuthentificationEntity();

    when(authRepo.findByUserEmailAndProviderId(testEmail, testProviderId)).thenReturn(auth);
    when(authMapper.modelToEntity(auth)).thenReturn(authEntity);
    when(authRepo.save(any())).thenReturn(auth);

    // Act
    tokenService.removeToken(userDetails);

    // Assert
    verify(authRepo).save(any());
    assertNull(authEntity.getRefreshToken());
  }

  @Test
    void removeToken_NullUserDetails_ThrowsBadRequestException() {
        // Arrange
        when(exceptionFactory.badRequest("badrequest.missing_token"))
                .thenReturn(new BadRequestException("Missing user details",""));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> tokenService.removeToken(null));
    }
}
