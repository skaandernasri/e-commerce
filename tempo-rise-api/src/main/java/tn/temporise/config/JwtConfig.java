package tn.temporise.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Base64;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@RequiredArgsConstructor
@Configuration
public class JwtConfig {
  private final ExceptionFactory exceptionFactory;
  @Value("classpath:private_key.pem")
  private Resource privateKeyResource;

  @Value("classpath:public_key.pem")
  private Resource publicKeyResource;

  @Bean
  public RSAPrivateKey rsaPrivateKey() throws IOException {
    String privateKeyContent = new String(privateKeyResource.getInputStream().readAllBytes())
        .replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "")
        .replaceAll("\\s", "");

    byte[] decodedKey = Base64.getDecoder().decode(privateKeyContent);
    try {
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  @Bean
  public RSAPublicKey rsaPublicKey() {
    try {
      String publicKeyContent = new String(publicKeyResource.getInputStream().readAllBytes())
          .replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "")
          .replaceAll("\\s", "");

      byte[] decodedKey = Base64.getDecoder().decode(publicKeyContent);
      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      return (RSAPublicKey) keyFactory.generatePublic(keySpec);

    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  @Bean
  public JwtEncoder jwtEncoder(RSAPrivateKey rsaPrivateKey, RSAPublicKey rsaPublicKey) {
    JWK jwk = new RSAKey.Builder(rsaPublicKey).privateKey(rsaPrivateKey).build();

    JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));

    return new NimbusJwtEncoder(jwkSource);
  }

  @Bean
  public JwtDecoder jwtDecoder(RSAPublicKey rsaPublicKey) {
    return NimbusJwtDecoder.withPublicKey(rsaPublicKey).build();
  }
}
