package tn.temporise.infrastructure.security.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.CustomUserDetails;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
  private final ExceptionFactory exceptionFactory;
  @Value("${jwt.secret.key}")
  private String secretKey;
  @Value("${jwt.access.token.expiration}")
  private Duration accessTokenExpiration;
  @Value("${jwt.refresh.token.expiration}")
  private Duration refreshTokenExpiration;
  @Value("${cookie.expiration}")
  private Duration cookieExpiration;
  @Value("${csv.access.token}")
  private String csvAccessSubject;
  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;

  public String extractSubject(String token) {
    Jwt jwt = jwtDecoder.decode(token);
    return jwt.getSubject();
  }

  public String extractEmail(String token) {
    Jwt jwt = jwtDecoder.decode(token);
    return jwt.getSubject();
  }

  public String extractId(String token) {
    Jwt jwt = jwtDecoder.decode(token);
    return jwt.getClaim("id").toString();

  }

  public Instant extractExpiration(String token) {
    Jwt jwt = jwtDecoder.decode(token);
    Instant expiration = jwt.getExpiresAt();
    if (expiration == null) {
      throw exceptionFactory.unauthorized("unauthorized.token_expired");
    }
    return expiration.atZone(ZoneId.systemDefault()).toInstant(); // Convert to local time
  }

  public Boolean isTokenExpired(String token) {
    Instant expiration = extractExpiration(token);
    return expiration != null && expiration.isBefore(Instant.now());
  }

  public String extractProviderId(String token) {
    Jwt jwt = jwtDecoder.decode(token);
    return jwt.getClaim("provider_id").toString();
  }

  public Collection<String> extractRoles(String token) {
    Jwt jwt = jwtDecoder.decode(token);
    return jwt.getClaim("roles");
  }

  public String extractUsername(String token) {
    Jwt jwt = jwtDecoder.decode(token);
    return jwt.getClaim("username");
  }

  // Generate Access Token
  public String generateAccessToken(CustomUserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("username", userDetails.username());
    claims.put("roles", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList()));
    claims.put("provider_id", userDetails.getProviderId());
    claims.put("id", userDetails.id());
    log.info("-------token values " + claims.values());
    return createToken(claims, userDetails.email(), accessTokenExpiration);
  }

  // Generate Refresh Token
  public String generateRefreshToken(CustomUserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("email", userDetails.email());
    claims.put("provider_id", userDetails.getProviderId());
    claims.put("username", userDetails.username());
    claims.put("roles", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList()));
    claims.put("id", userDetails.id());

    return createToken(claims, userDetails.email(), refreshTokenExpiration);
  }

  private String createToken(Map<String, Object> claims, String subject, Duration expiration) {
    var jwsHeader = JwsHeader.with(SignatureAlgorithm.RS256).build();
    Instant issuedAt = Instant.now();
    Instant expiresAt = issuedAt.plus(expiration);

    log.info("🔹 Issued At: {}", issuedAt);
    log.info("🔹 Expiration At: {}", expiresAt);
    log.info("🔹 Expiration Duration (ms): {}", expiration);
    JwtClaimsSet claimsSet = JwtClaimsSet.builder().issuer("tempo-rise") // Set your issuer
        .subject(subject).claims(claimsMap -> claimsMap.putAll(claims)) // Use a lambda to add
                                                                        // claims
        .issuedAt(Instant.now()).expiresAt(Instant.now().plus(expiration)).build();

    return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet)).getTokenValue();
  }

  public Boolean validateToken(String token, CustomUserDetails userDetails) throws Exception {
    final String email = extractEmail(token);
    return (email.equals(userDetails.email()) && !isTokenExpired(token));
  }

  public boolean isTokenStructureValid(String token) {

    if (token == null) {
      log.info("Token is null");
      throw exceptionFactory.notFound("notfound.token");
    }

    String[] parts = token.split("\\.");
    if (parts.length != 3) {
      log.info("Token structure is not valid");
      throw exceptionFactory.unauthorized("unauthorized.mal_formed");
    }

    // Verify Base64URL decoding works
    Base64.getUrlDecoder().decode(parts[0]);
    Base64.getUrlDecoder().decode(parts[1]);
    return true;
  }

  // Extract Claims from Token
  public Map<String, Object> extractClaims(String token) {
    Jwt jwt = jwtDecoder.decode(token);
    return jwt.getClaims();
  }

  public void setJwtCookie(HttpServletResponse response, String jwtToken) {
    // ResponseCookie cookie= ResponseCookie.from("jwt",
    // jwtToken).httpOnly(true).secure(true).sameSite("None").path("/").maxAge(cookieExpiration.toSeconds()).build();
    log.info("cookie expired in {}", cookieExpiration);
    Cookie jwtCookie = new Cookie("jwt", jwtToken);
    jwtCookie.setHttpOnly(true); // Makes the cookie inaccessible to JavaScript
    jwtCookie.setSecure(true); // Cookie is sent only over HTTPS
    jwtCookie.setPath("/"); // Available across all paths of the application
    jwtCookie.setMaxAge((int) cookieExpiration.toSeconds()); // Expiration time for the cookie
    log.info("JWT cookie set: {}", jwtCookie);
    jwtCookie.setAttribute("SameSite", "None"); // Allow cross-origin requests

    // Add the cookie to the response
    response.addCookie(jwtCookie);
    // response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }


  public void removeJwtCookie(HttpServletResponse response) {
    // ResponseCookie cookie=
    // ResponseCookie.from("jwt").httpOnly(true).secure(true).sameSite("None").path("/").maxAge(0).build();
    Cookie jwtCookie = new Cookie("jwt", null);
    jwtCookie.setHttpOnly(true);
    jwtCookie.setSecure(true); // Set to true if using HTTPS
    jwtCookie.setPath("/");
    jwtCookie.setMaxAge(0); // Expire the cookie immediately
    response.addCookie(jwtCookie);
    // response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  public String getJwtFromCookies(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("jwt".equals(cookie.getName())) {
          log.info("Token found in cookie: {}", cookie.getValue());
          return cookie.getValue();
        }
      }
    }
    log.info("Token not found in cookies");
    return null;

  }

  public String extractJwtFromRequest(HttpServletRequest request) {
    // Try Authorization header first
    String jwt = getJwtFromAuthorizationHeader(request);

    // Fallback to cookies if not found in header
    if (jwt == null) {
      jwt = getJwtFromCookies(request);
      if (jwt != null) {
        log.debug("JWT found in cookies");
      }
    }

    return jwt;
  }

  public String getJwtFromAuthorizationHeader(HttpServletRequest request) {
    final String authorizationHeader = request.getHeader("Authorization");
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      log.debug("JWT found in Authorization header");
      return authorizationHeader.substring(7);
    }
    return null;
  }

  public String createMetaToken() {
    var jwsHeader = JwsHeader.with(SignatureAlgorithm.RS256).build();
    JwtClaimsSet claimsSet = JwtClaimsSet.builder().issuer("tempo-rise") // Set your issuer
        .subject(csvAccessSubject)// Use a lambda to add
        .issuedAt(Instant.now()).build();
    return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet)).getTokenValue();
  }

}
