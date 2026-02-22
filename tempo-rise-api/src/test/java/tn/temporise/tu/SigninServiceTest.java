package tn.temporise.tu;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.service.CustomUserDetailsService;
import tn.temporise.application.service.SigninService;
import tn.temporise.application.service.TokenService;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.CustomUserDetails;
import tn.temporise.domain.model.SigninUserRequest;
import tn.temporise.domain.model.TokenResponse;
import tn.temporise.domain.port.PanierRepo;
import tn.temporise.domain.port.WhishlistRepo;
import tn.temporise.infrastructure.security.utils.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SigninServiceTest {

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private ExceptionFactory exceptionFactory;

  @Mock
  private HttpServletResponse response;

  @Mock
  private CustomUserDetailsService userDetailsService;

  @Mock
  private TokenService tokenService;

  @Mock
  private AuthenticationManager authManager;

  @Mock
  private HttpServletRequest request;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private PanierRepo panierRepo;

  @Mock
  private WhishlistRepo whishlistRepo;

  @InjectMocks
  private SigninService signinService;

  @Test
  void signinUser_ValidCredentials_ReturnsTokens() {
    // Arrange
    String password = "Password1220.";
    String encodedPassword = passwordEncoder.encode(password);
    SigninUserRequest request = new SigninUserRequest("test@example.com", password, "token");
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(CustomUserDetails.builder()
        .id(1L).email("user@email.com").username("user").password(encodedPassword)
        .authorities(Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")))
        .providerId("0").isverified(true).isEnabled(true).isAccountNonExpired(true)
        .isAccountNonLocked(true).isCredentialsNonExpired(true).build());
    when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
    when(jwtUtil.generateAccessToken(any())).thenReturn("access-token");
    when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh-token");

    // Act
    TokenResponse result = signinService.signinUser(request);

    // Assert
    assertNotNull(result);
    assertEquals("access-token", result.getToken());
    verify(tokenService, times(1)).saveToken(anyString(), anyString(), anyString());
  }

  @Test
  void signinUser_InvalidCredentials_ThrowsBadRequest() {
    // Arrange

    SigninUserRequest request = new SigninUserRequest(null, null, null);
    when(exceptionFactory.badRequest("badrequest.invalid_request"))
        .thenReturn(new BadRequestException("", ""));

    // Act & Assert
    assertThrows(BadRequestException.class, () -> signinService.signinUser(request));
  }


  @Test
  void validateSigninRequest_NullEmail_ThrowsBadRequest() {
    // Arrange
    SigninService service =
        new SigninService(null, exceptionFactory, null, null, null, null, null, null, null);
    when(exceptionFactory.badRequest(anyString())).thenReturn(new BadRequestException("", ""));

    // Act & Assert
    assertThrows(BadRequestException.class,
        () -> service.validateSigninRequest(new SigninUserRequest(null, "pass", "token")));
  }
}
