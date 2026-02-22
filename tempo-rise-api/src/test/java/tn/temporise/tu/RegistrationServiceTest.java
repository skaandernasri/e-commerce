package tn.temporise.tu;

import freemarker.template.TemplateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.ConflictException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.RegMapper;
import tn.temporise.application.service.RegistrationService;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.Authentification;
import tn.temporise.domain.model.SignupUserRequest;
import tn.temporise.domain.model.TypeAuthentification;
import tn.temporise.domain.model.UtilisateurModel;
import tn.temporise.domain.port.*;

import jakarta.mail.MessagingException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

  @Mock
  private UserRepo userRepo;
  @Mock
  private RegMapper regMapper;

  @Mock
  private AuthRepo authRepo;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private ExceptionFactory exceptionFactory;

  @Mock
  private EmailInterface emailInterface;

  @Mock
  private PanierRepo panierRepo;

  @Mock
  private WhishlistRepo whishlistRepo;

  @InjectMocks
  private RegistrationService registrationService;

  @Test
  void register_NewUser_Success() throws MessagingException, IOException, TemplateException {
    // Arrange
    SignupUserRequest request = new SignupUserRequest();
    request.setEmail("test@example.com");
    request.setPassword("Password123.");
    request.setNom("Test User");
    UtilisateurModel user = UtilisateurModel.builder().id(1L).email(request.getEmail())
        .password(request.getPassword()).nom(request.getNom()).build();
    Authentification authModel = Authentification.builder().id(1L).user(user)
        .password(passwordEncoder.encode(request.getPassword())).type(TypeAuthentification.EMAIL)
        .providerId("0").build();

    when(userRepo.findByEmail(anyString()))
        .thenThrow(new NotFoundException("notfound.user", "4043"));
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123.");
    when(regMapper.toModel(request)).thenReturn(user);
    when(userRepo.save(user)).thenReturn(user);
    registrationService.register(request);

    // Assert
    verify(userRepo, times(1)).save(any());
    verify(authRepo, times(1)).save(any());
    verify(emailInterface, times(1)).sendVerificationEmail(anyString());
  }

  @Test
  void register_ExistingUserWithAuth_ThrowsConflict() {
    // Arrange
    SignupUserRequest request = new SignupUserRequest();
    request.setEmail("test@example.com");
    request.setPassword("Password123.");
    request.setNom("Test User");
    UtilisateurModel existingUser = UtilisateurModel.builder().email("test@example.com").build();
    when(userRepo.findByEmail(anyString())).thenReturn(existingUser);
    when(exceptionFactory.conflict(anyString())).thenReturn(new ConflictException("", ""));

    // Act & Assert
    assertThrows(ConflictException.class, () -> registrationService.register(request));
  }

  @Test
  void register_ExistingUserWithoutAuth_AddsAuth()
      throws MessagingException, IOException, TemplateException {
    // Arrange
    SignupUserRequest request = new SignupUserRequest();
    request.setEmail("test@example.com");
    request.setPassword("Password123.");
    request.setNom("Test User");
    UtilisateurModel existingUser = UtilisateurModel.builder().email("test@example.com").build();
    when(userRepo.findByEmail(anyString())).thenReturn(existingUser);
    when(authRepo.findByUserEmailAndProviderId(anyString(), anyString()))
        .thenThrow(new NotFoundException("", ""));
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

    // Act
    registrationService.register(request);

    // Assert
    verify(authRepo, times(1)).save(any(Authentification.class));
    verify(emailInterface, times(1)).sendVerificationEmail(anyString());
  }

  @Test
    void validatePassword_EmptyPassword_ThrowsBadRequest() {
        // Arrange
        when(exceptionFactory.badRequest(anyString())).thenReturn(new BadRequestException("",""));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> registrationService.validatePassword(""));
    }

  @Test
    void validatePassword_WeakPassword_ThrowsPasswordException() {
        // Arrange
        when(exceptionFactory.badRequest(anyString())).thenReturn(new BadRequestException("",""));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> registrationService.validatePassword("weak"));
    }
}
