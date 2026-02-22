package tn.temporise.infrastructure.security.utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tn.temporise.application.exception.auth.UnauthorizedException;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.service.CustomUserDetailsService;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.CustomUserDetails;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
  @Autowired
  private final CustomUserDetailsService userDetailsService;
  @Autowired
  private final JwtUtil jwtUtil;
  private final ExceptionFactory exceptionFactory;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) {
    try {
      log.info("passed by doFilterInternal");
      // System.out.println("generate jwt meta token: "+ jwtUtil.createMetaToken());
      // 1. Extract JWT from request
      String jwt = jwtUtil.extractJwtFromRequest(request);

      if (jwt == null) {
        chain.doFilter(request, response);
        return;
      }
      log.info("JWT found in request: {}", jwt);
      // 2. Validate and process JWT
      processJwtToken(jwt, request, response);
      log.info("JWT processed successfully");
      // 3. Continue the filter chain
      chain.doFilter(request, response);

    } catch (UnauthorizedException | AuthorizationDeniedException | BadRequestException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }



  private void processJwtToken(String jwt, HttpServletRequest request,
      HttpServletResponse response) {
    try {
      // 1. Validate token structure first
      if (!jwtUtil.isTokenStructureValid(jwt)) {
        log.info("Token structure is not valid");
        throw exceptionFactory.unauthorized("unauthorized.token_malformed");
      }

      // validate expiration
      if (jwtUtil.isTokenExpired(jwt)) {
        log.info("Token is expired");
        throw exceptionFactory.unauthorized("unauthorized.token_expired");
      }

      // 2. Extract claims
      String email = jwtUtil.extractEmail(jwt);
      log.info("Email: {}", email);
      String providerId = jwtUtil.extractProviderId(jwt);
      log.info("ProviderId: {}", providerId);

      // 3. Load user details
      CustomUserDetails userDetails =
          userDetailsService.loadUserByUsername(email + ";" + providerId);

      // 4. Validate token against user details
      if (!jwtUtil.validateToken(jwt, userDetails)) {
        log.info("Token is not valid for user: {}", email);
        jwtUtil.removeJwtCookie(response);
        throw exceptionFactory.unauthorized("unauthorized.invalid_token");
      }

      // Only create authentication if not already authenticated
      if (SecurityContextHolder.getContext().getAuthentication() == null
          || SecurityContextHolder.getContext().getAuthentication().getPrincipal() == null) {
        // 5. Create authentication
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // 6. Set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("Authenticated user: {}", email);
        log.info("Authenticated user Authorities: {}",
            SecurityContextHolder.getContext().getAuthentication().getAuthorities());
      }
    } catch (NotFoundException | BadRequestException | UnauthorizedException e) {
      jwtUtil.removeJwtCookie(response);
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String requestURI = request.getRequestURI();
    String method = request.getMethod();

    // Add public endpoints here
    log.debug("Checking if request should be filtered: " + requestURI);
    boolean shouldNotFilter = requestURI.startsWith("/v1/auth/signin") && method.equals("POST")
        || requestURI.startsWith("/v1/auth/signin/google") && method.equals("POST")
        || requestURI.startsWith("/v1/auth/signup") && method.equals("POST")
        || requestURI.startsWith("/actuator") || requestURI.startsWith("/v1/sendVerificationEmail")
        || requestURI.equals("/v1/suiviClient") && method.equals("POST")
        || requestURI.startsWith("/v1/activate") || requestURI.startsWith("/oauth2/")
        || requestURI.startsWith("/swagger-ui/") || requestURI.startsWith("/v3/api-docs")
        || requestURI.startsWith("/swagger-resources/")
        || requestURI.startsWith("/swagger-ui/index.html")
        || requestURI.startsWith("/v1/sendResetPasswordEmail")
        || requestURI.startsWith("/v1/validate-reset-token")
        || requestURI.startsWith("/favicon.ico") || requestURI.startsWith("/v1/reset-password")
        || requestURI.startsWith("/v1/auth/logout")
        || requestURI.startsWith("/v1/produits") && method.equals("GET")
        || requestURI.startsWith("/v1/avis") && method.equals("GET")
        || requestURI.startsWith("/v1/imageProduit") && method.equals("GET")
        || requestURI.startsWith("/v1/categories") && method.equals("GET")
        || requestURI.startsWith("/v1/promotions") && method.equals("GET")
        || requestURI.startsWith("/v1/articles") && method.equals("GET")
        || requestURI.startsWith("/v1/codePromo") && method.equals("GET")
        || requestURI.startsWith("/v1/imageByProductId") && method.equals("GET")
        || requestURI.startsWith("/v1/promotionsByProductId") && method.equals("GET")
        || requestURI.startsWith("/v1/avisByProductId") && method.equals("GET")
        || requestURI.startsWith("/v1/variant/") && method.equals("GET")
        || requestURI.startsWith("/v1/imageBlogPost/") && method.equals("GET")
        || requestURI.startsWith("/v1/imageByBlogPostId/") && method.equals("GET")
        || requestURI.startsWith("/v1/recommendation")
        || requestURI.startsWith("/v1/utilisateur/anonyme")
        || requestURI.startsWith("/v1/cookieConsent")
        || requestURI.startsWith("/v1/contacts") && method.equals("POST")
        || requestURI.startsWith("/v1/avisByUserId") && method.equals("GET")
        || requestURI.startsWith("/v1/section/type/") && method.equals("GET")
        || requestURI.startsWith("/v1/section/pageType/") && method.equals("GET")
        || requestURI.matches("/v1/commandes/\\d+") && method.equals("GET")
        || requestURI.startsWith("/v1/configurationGlobal") && method.equals("GET")
        || requestURI.startsWith("/v1/commande/guest") && method.equals("POST")
        || requestURI.startsWith("/v1/konnect/webhook") && method.equals("GET")
        || requestURI.startsWith("/v1/paiement") && method.equals("GET")
        || requestURI.startsWith("/v1/commandes/paymentRef") && method.equals("GET")
        || requestURI.startsWith("/v1/adresses") && method.equals("POST")
        || requestURI.startsWith("/v1/adresses/utilisateur") && method.equals("GET")
        || requestURI.startsWith("/v1/factures/commande/") && method.equals("GET")
        || requestURI.startsWith("/v1/meta/") && (method.equals("GET") || method.equals("POST"))
        || requestURI.startsWith("/v1/mergeCommande") && method.equals("POST")
        || requestURI.startsWith("/uploads/")
        || requestURI.startsWith("/v1/meta/download/catalogueCsv") && method.equals("GET");
    log.info("Should not filter: " + shouldNotFilter);
    return shouldNotFilter;

  }


}
