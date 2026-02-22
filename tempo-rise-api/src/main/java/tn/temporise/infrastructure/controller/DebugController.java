package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.infrastructure.security.utils.JwtUtil;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {
  private final JwtUtil jwtUtil; // Inject your JwtUtil instead

  @GetMapping("/auth")
  public ResponseEntity<?> debugAuth(Authentication authentication) {
    return ResponseEntity
        .ok(Map.of("name", authentication.getName(), "authorities", authentication.getAuthorities()
            .stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList())));
  }

  @GetMapping("/token")
  public ResponseEntity<?> debugToken(@CookieValue("jwt") String jwt) {
    try {
      // Use your existing JwtUtil to decode
      String email = jwtUtil.extractEmail(jwt);
      String providerId = jwtUtil.extractProviderId(jwt);
      Collection<String> roles = jwtUtil.extractRoles(jwt);

      return ResponseEntity.ok(Map.of("email", email, "providerId", providerId, "roles", roles));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
    }
  }
}
