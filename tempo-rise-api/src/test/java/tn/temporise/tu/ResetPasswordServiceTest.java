package tn.temporise.tu;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.temporise.application.exception.auth.UnauthorizedException;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.service.RegistrationService;
import tn.temporise.application.service.ResetPasswordService;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.config.ZoneConfig;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.AuthRepo;
import tn.temporise.domain.port.UserRepo;
import tn.temporise.infrastructure.security.utils.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceTest {

  @Mock
  private UserRepo userRepo;
  @Mock
  private ZoneConfig zoneConfig;

  @Mock
  private ExceptionFactory exceptionFactory;

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private RegistrationService registrationService;

  @Mock
  private HttpServletResponse response;

  @Mock
  private HttpServletRequest request;

  @Mock
  private AuthRepo authRepo;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private ResetPasswordService resetPasswordService;

  @Test
  void validateResetToken_ValidToken_ReturnsAccessToken() {
    // Arrange
    String token = "valid-token";
    UtilisateurModel user = UtilisateurModel.builder().email("test@example.com")
        .resetpasswordexpiresat(LocalDateTime.now(ZoneId.of("Europe/Paris")).plusSeconds(86400))
        .build();
    when(userRepo.findByResetPasswordToken(anyString())).thenReturn(user);
    // when(zoneConfig.getTimezone()).thenReturn("Europe/Paris");
    // Act
    resetPasswordService.validateResetToken(token);

    // Assert
  }

  @Test
  void validateResetToken_ExpiredToken_ThrowsConflict() {
    // Arrange
    String token = "expired-token";
    UtilisateurModel user = UtilisateurModel.builder().email("test@example.com")
        .resetpasswordexpiresat(LocalDateTime.now(ZoneId.of("Europe/Paris")).minusSeconds(86400))
        .build();
    // when(zoneConfig.getTimezone()).thenReturn("Europe/Paris");

    when(userRepo.findByResetPasswordToken(anyString())).thenReturn(user);
    when(exceptionFactory.unauthorized(anyString())).thenReturn(new UnauthorizedException("", ""));

    // Act & Assert
    assertThrows(UnauthorizedException.class, () -> resetPasswordService.validateResetToken(token));
  }

  @Test
  void resetPassword_ValidRequest_UpdatesPassword() {
    // Arrange
    String token = "token";
    PasswordResetTokenRequest request = new PasswordResetTokenRequest("Skander123", "Skander123");
    // doNothing().when(registrationService).validatePassword(anyString());

    UtilisateurModel user = UtilisateurModel.builder().email("test@example.com").build();
    when(userRepo.findByResetPasswordToken(anyString())).thenReturn(user);

    Authentification auth = Authentification.builder().id(1L).build();
    when(authRepo.findByUserEmailAndProviderId(anyString(), anyString())).thenReturn(auth);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    // Act
    resetPasswordService.resetPassword(token, request);

    // Assert
    verify(userRepo, times(1)).save(any());
    verify(authRepo, times(1)).save(any());
  }

  @Test
  void resetPassword_PasswordMismatch_ThrowsBadRequest() {
    // Arrange
    String token = "token";
    PasswordResetTokenRequest request = new PasswordResetTokenRequest("newPass", "different");
    when(exceptionFactory.badRequest("badrequest.password.mismatch")).thenReturn(
        new BadRequestException("badrequest.password.mismatch", "Passwords don't match"));

    // Act & Assert
    assertThrows(BadRequestException.class,
        () -> resetPasswordService.resetPassword(token, request));
  }

  @Test
  void isTokenExpired_Expired_ReturnsTrue() {
    // Arrange
    LocalDateTime expired = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(86400);
    // when(zoneConfig.getTimezone()).thenReturn("Europe/Paris");

    // Act
    boolean result = resetPasswordService.isTokenExpired(expired);

    // Assert
    assertTrue(result);
  }
}
