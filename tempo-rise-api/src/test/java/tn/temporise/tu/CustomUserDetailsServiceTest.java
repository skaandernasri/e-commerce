package tn.temporise.tu;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.service.CustomUserDetailsService;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.AuthRepo;
import tn.temporise.domain.port.UserRepo;
import tn.temporise.infrastructure.security.utils.JwtUtil;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

  @Mock
  private UserRepo userRepo;

  @Mock
  private AuthRepo authenticationRepo;

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private ExceptionFactory exceptionFactory;

  @InjectMocks
  private CustomUserDetailsService customUserDetailsService;

  private final String TEST_EMAIL = "test@example.com";
  private final String TEST_NAME = "Test User";
  private final String TEST_PASSWORD = "password";
  private final String PROVIDER_ID = "1";
  private final String INVALID_USERNAME = "invalid";

  @BeforeEach
  void setUp() {
    // Common setup if needed
  }

  @Test
  void loadUserByUsername_ValidUsername_ReturnsUserDetails() {
    // Arrange
    String username = TEST_EMAIL + ";" + PROVIDER_ID;
    UtilisateurModel user =
        UtilisateurModel.builder().email(TEST_EMAIL).nom(TEST_NAME.split(" ")[1])
            .prenom(TEST_NAME.split(" ")[0]).roles(Set.of(Role.CLIENT)).build();
    Authentification auth = Authentification.builder().user(user).type(TypeAuthentification.EMAIL)
        .password(TEST_PASSWORD).providerId(PROVIDER_ID).build();

    when(userRepo.findByEmail(TEST_EMAIL)).thenReturn(user);
    when(authenticationRepo.findByUserEmailAndProviderId(TEST_EMAIL, PROVIDER_ID)).thenReturn(auth);

    // Act
    CustomUserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

    // Assert
    assertNotNull(userDetails);
    assertEquals(TEST_EMAIL, userDetails.email());
    assertEquals(TEST_NAME, userDetails.username());
    assertEquals(TEST_PASSWORD, userDetails.getPassword());
    assertEquals(PROVIDER_ID, userDetails.getProviderId());
    assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENT")));
  }

  @Test
    void loadUserByUsername_InvalidFormat_ThrowsUsernameNotFoundException() {
        // Arrange
        when(exceptionFactory.badRequest("badrequest.invalid_input")).thenReturn(new BadRequestException("Invalid input","4009"));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            customUserDetailsService.loadUserByUsername(INVALID_USERNAME);
        });
    }

  @Test
  void getUserDetails_ValidInput_ReturnsUserDetails() {
    // Arrange
    UtilisateurModel user = UtilisateurModel.builder().email(TEST_EMAIL).nom(TEST_NAME)
        .roles(Set.of(Role.ADMIN)).build();
    Authentification auth = Authentification.builder().user(user).type(TypeAuthentification.EMAIL)
        .password(TEST_PASSWORD).providerId(PROVIDER_ID).build();

    when(userRepo.findByEmail(TEST_EMAIL)).thenReturn(user);
    when(authenticationRepo.findByUserEmailAndProviderId(TEST_EMAIL, PROVIDER_ID)).thenReturn(auth);

    // Act
    CustomUserDetails userDetails =
        customUserDetailsService.loadUserByUsername(TEST_EMAIL + ";" + PROVIDER_ID);

    // Assert
    assertNotNull(userDetails);
    assertEquals(TEST_EMAIL, userDetails.email());
    assertEquals(TEST_PASSWORD, userDetails.getPassword());
    assertEquals(PROVIDER_ID, userDetails.getProviderId());
    assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
  }

  @Test
    void getUserDetails_NullEmail_ThrowsBadRequestException() {
        // Arrange
        when(exceptionFactory.badRequest("badrequest.invalid_input")).thenReturn(new BadRequestException("Invalid input","4009"));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            customUserDetailsService.loadUserByUsername(PROVIDER_ID);
        });
    }

  @Test
    void getUserDetails_EmptyProviderId_ThrowsBadRequestException() {
        // Arrange
        when(exceptionFactory.badRequest("badrequest.invalid_input")).thenReturn(new BadRequestException("Invalid input","4009"));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            customUserDetailsService.loadUserByUsername(TEST_EMAIL);
        });
    }

  @Test
    void getUserDetails_UserNotFound_ThrowsNotFoundException() {
        // Arrange
        when(userRepo.findByEmail(TEST_EMAIL)).thenThrow(new NotFoundException("User not found","4043"));

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(TEST_EMAIL+";"+PROVIDER_ID);
        });
    }

  @Test
  void getUserDetails_AuthNotFound_ThrowsNotFoundException() {
    // Arrange
    UtilisateurModel user = UtilisateurModel.builder().email(TEST_EMAIL).nom(TEST_NAME)
        .roles(Collections.emptySet()).build();
    when(userRepo.findByEmail(TEST_EMAIL)).thenReturn(user);
    when(authenticationRepo.findByUserEmailAndProviderId(TEST_EMAIL, PROVIDER_ID))
        .thenThrow(new NotFoundException("Authentication method not found", "4046"));

    // Act & Assert
    assertThrows(NotFoundException.class, () -> {
      customUserDetailsService.loadUserByUsername(TEST_EMAIL + ";" + PROVIDER_ID);
    });
  }
}
