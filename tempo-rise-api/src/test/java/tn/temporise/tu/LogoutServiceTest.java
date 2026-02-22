package tn.temporise.tu;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import tn.temporise.application.exception.auth.UnauthorizedException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.application.service.LogoutService;
import tn.temporise.application.service.TokenService;
import tn.temporise.domain.model.CustomUserDetails;
import tn.temporise.infrastructure.security.utils.JwtRequestFilter;
import tn.temporise.infrastructure.security.utils.JwtUtil;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {
  @Mock
  private TokenService tokenService;

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private ExceptionFactory exceptionFactory;

  @Mock
  private JwtRequestFilter jwtRequestFilter;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private Authentication authentication;

  @InjectMocks
  private LogoutService logoutService;

  private final String TEST_TOKEN = "test.jwt.token";
  private final String TEST_EMAIL = "test@example.com";
  private final String TEST_PROVIDER_ID = "1";

  @BeforeEach
  void setUp() {
    reset(tokenService, jwtUtil, exceptionFactory, jwtRequestFilter, request, response,
        authentication);
  }

  @Test
  void logout_WithValidToken_ShouldRevokeTokenAndClearContext() {
    // Arrange
    Cookie cookie = new Cookie("jwt", TEST_TOKEN);
    when(jwtUtil.getJwtFromCookies(request)).thenReturn(cookie.getValue());
    when(jwtUtil.isTokenStructureValid(cookie.getValue())).thenReturn(true);
    when(jwtUtil.isTokenExpired(cookie.getValue())).thenReturn(false);
    when(jwtUtil.extractEmail(TEST_TOKEN)).thenReturn(TEST_EMAIL);
    when(jwtUtil.extractProviderId(TEST_TOKEN)).thenReturn(TEST_PROVIDER_ID);

    // Act
    logoutService.logout(request, response, authentication);

    // Assert
    verify(jwtUtil).getJwtFromCookies(request);
    verify(tokenService).removeToken(any(CustomUserDetails.class));
    verify(jwtUtil).removeJwtCookie(response);
    // assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
    void logout_WithoutToken_ShouldStillClearContext() {
        // Arrange
        //when(request.getCookies()).thenReturn(null);
        when(jwtUtil.getJwtFromCookies(request)).thenReturn(null);

        // Act

        logoutService.logout(request, response, authentication);

        // Assert
        verify(jwtUtil,times(1)).getJwtFromCookies(request);
        verify(tokenService, never()).removeToken(any());
        verify(jwtUtil, never()).removeJwtCookie(response);
        //assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

  @Test
  void logout_WithInvalidToken_ShouldHandleGracefully() {
    // Arrange
    Cookie cookie = new Cookie("jwt", "invalid.token");
    // when(logoutService.getTokenFromCookies(request)).thenReturn(new Cookie[]{cookie});
    when(jwtUtil.getJwtFromCookies(request)).thenReturn(cookie.getValue());
    when(jwtUtil.isTokenStructureValid(cookie.getValue()))
        .thenThrow(new UnauthorizedException("Invalid token", ""));

    // Act & Assert
    assertThrows(UnauthorizedException.class,
        () -> logoutService.logout(request, response, authentication));
  }


}
