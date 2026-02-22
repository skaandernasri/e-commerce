package tn.temporise.config;


import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tn.temporise.application.exception.auth.CustomAccessDeniedHandler;
import tn.temporise.application.service.LogoutService;
import tn.temporise.infrastructure.security.utils.JwtRequestFilter;
import tn.temporise.application.service.CustomUserDetailsService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Autowired
  @Lazy
  private final CustomUserDetailsService userDetailsService;

  @Autowired
  @Lazy
  private final JwtRequestFilter jwtRequestFilter;

  @Autowired
  @Lazy
  private final JwtConfig jwtConfig;

  private final LogoutService logoutService;
  private final CustomAccessDeniedHandler accessDeniedHandler;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.POST, "/v1/auth/signin").permitAll()
        .requestMatchers("/v1/auth/signup").permitAll().requestMatchers("/v1/auth/signin/google")
        .permitAll().requestMatchers("/ws/**").authenticated()
        .requestMatchers(HttpMethod.POST, "/v1/contacts", "/v1/commande/guest", "/v1/adresses",
            "/v1/mergeCommande")
        .permitAll()
        .requestMatchers(HttpMethod.GET, "/v1/paiement", "/v1/konnect/webhook",
            "/v1/commandes/paymentRef", "/v1/configurationGlobal")
        .permitAll()
        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/oauth2/**", "/swagger-resources/**",
            "/webjars/**", "/v1/sendVerificationEmail", "/v1/activate",
            "/v1/sendResetPasswordEmail", "/v1/validate-reset-token", "/v1/reset-password",
            "/favicon.ico", "/static/**", "/tempo-rise/api/v1/**", "/actuator/**",
            "/v1/suiviClient", "/v1/recommendation", "/v1/utilisateur/anonyme/**",
            "/v1/utilisateur/anonyme", "/v1/cookieConsent", "/uploads/**", "/v1/meta/**")
        .permitAll()
        .requestMatchers(HttpMethod.GET, "/v1/produits/**", "/v1/avis/**", "/v1/imageProduit/**",
            "/v1/categories/**", "/v1/promotions/**", "/v1/codePromo/**", "/v1/imageByProductId/**",
            "/v1/promotionsByProductId/**", "/v1/avisByProductId/**", "/v1/avisByUserId/**",
            "/v1/variant/**", "/v1/articles/**", "/v1/imageByBlogPostId/**", "/uploads/**",
            "/v1/produits/byIds/**", "/v1/section/type/**", "/v1/section/pageType/**",
            "/v1/adresses/utilisateur", "/v1/commandes/**", "/v1/factures/commande/**")
        .permitAll().anyRequest().authenticated()

    ).oauth2ResourceServer(
        oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtConfig.jwtDecoder(jwtConfig.rsaPublicKey()))
            .jwtAuthenticationConverter(jwtAuthenticationConverter())))

        .logout(logout -> logout.logoutUrl("/v1/auth/logout")
            .logoutRequestMatcher(new AntPathRequestMatcher("/v1/auth/logout", "POST")) // Explicitly
                                                                                        // match
                                                                                        // POST only
            .addLogoutHandler(logoutService)
            // .deleteCookies("jwt", "JSESSIONID", "remember-me")
            .clearAuthentication(true).invalidateHttpSession(true)
            .logoutSuccessHandler((request, response, authentication) -> {
              response.setStatus(HttpServletResponse.SC_OK);
              response.setContentType("application/json");
              response.getWriter()
                  .write("{\"code\":\"200\",\"message\":\"Logged out successfully\"}");
            })

        )
        // Disable concurrent session control
        .securityContext(securityContext -> securityContext.requireExplicitSave(false))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        // Custom filter
        .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)

        .exceptionHandling(handling -> handling.accessDeniedHandler(accessDeniedHandler));
    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }


  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(
        List.of("https://temposphere.tn", "https://www.temposphere.tn", "http://localhost:4200"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    configuration.setExposedHeaders(List.of("Authorization"));
    // configuration.addAllowedOriginPattern("*");


    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
    converter.setAuthorityPrefix("ROLE_");
    converter.setAuthoritiesClaimName("roles");

    JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
    return jwtConverter;
  }
}
