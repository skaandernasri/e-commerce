package tn.temporise.application.service;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.exception.domain.ProviderException;
import tn.temporise.application.mapper.AuthMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.AuthRepo;
import tn.temporise.domain.port.PanierRepo;
import tn.temporise.domain.port.UserRepo;
import tn.temporise.domain.port.WhishlistRepo;
import tn.temporise.infrastructure.persistence.entity.AuthentificationEntity;
import tn.temporise.infrastructure.security.utils.JwtUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
  private final CustomUserDetailsService userDetailsService;
  private final JwtUtil jwtUtil;
  private final ExceptionFactory exceptionFactory;
  private final HttpServletResponse response;
  private final UserRepo userRepo;
  private final AuthRepo authRepo;
  private final AuthMapper authMapper;
  private final PanierRepo panierRepo;
  private final HttpServletRequest request;
  private final WhishlistRepo whishlistRepo;
  private final MetaPixelService metaPixelService;
  @Value("${uploadDir}")
  private String uploadDir;

  public TokenResponse signinGoogle(String idToken) {
    try {
      if (idToken == null || idToken.isEmpty()) {
        throw exceptionFactory.badRequest("badrequest.missing_token");
      }
      log.info("Google signin with token: {}", idToken);
      JWT jwt = JWTParser.parse(idToken);
      JWTClaimsSet claimsSet = jwt.getJWTClaimsSet();
      log.info("all claimsSet data in google: {}", claimsSet);
      String email = claimsSet.getStringClaim("email");
      String prenom = claimsSet.getStringClaim("given_name");
      String nom = claimsSet.getStringClaim("family_name");
      String picture = claimsSet.getStringClaim("picture");
      if (email == null || email.isEmpty()) {
        log.error("Email is null or empty");
        throw exceptionFactory.badRequest("badrequest.missing_email");
      }
      byte[] pictureBytes = null;
      if (picture != null && !picture.isEmpty()) {
        pictureBytes = downloadImageFromUrl(picture);
      }

      UtilisateurModel savedUser = findOrCreateUser(UtilisateurModel.builder().email(email).nom(nom)
          .prenom(prenom).image(pictureBytes).roles(Collections.singleton(Role.CLIENT)).build());
      CustomUserDetails userDetails = userDetailsService.loadUserByUsername(email + ";" + "1");
      log.info("Google signin User details: {}", userDetails);
      // Authenticate the user
      UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(userDetails, null, // No credentials (OAuth2
                                                                     // doesn't need a password)
              userDetails.getAuthorities());
      try {
        panierRepo.findByUtilisateurId(savedUser.id());
      } catch (NotFoundException ignore) {
        panierRepo.save(Panier.builder().utilisateur(savedUser).build());
      }
      try {
        whishlistRepo.findByUserId(savedUser.id());

      } catch (NotFoundException ignore) {
        whishlistRepo.save(Whishlist.builder().utilisateur(savedUser).build());
      }
      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authToken);

      String accessToken = jwtUtil.generateAccessToken(userDetails);

      jwtUtil.setJwtCookie(response, accessToken);
      log.info(" Google signin successful. Access token: " + response.getHeaderNames());
      TokenResponse token = new TokenResponse();
      token.setToken(accessToken);
      return token;

    } catch (ParseException e) {
      log.warn("Google token parsing failed", e);
      throw exceptionFactory.providerException("badrequest.invalid_token");
    } catch (BadRequestException | ProviderException | NotFoundException e) {
      log.warn("Google signin failed with client error", e);
      throw e;
    } catch (AuthenticationException e) {
      log.warn("Authentication failed: {}", e.getMessage());
      throw exceptionFactory.unauthorized("unauthorized.account_disabled");
    } catch (Exception e) {
      log.error("Google signin failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Google signin failed" + e.getMessage());
    }
  }

  private byte[] downloadImageFromUrl(String imageUrl) throws IOException, URISyntaxException {
    URL url = new URI(imageUrl).toURL();
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.setConnectTimeout(5000);
    connection.setReadTimeout(5000);

    try {
      int responseCode = connection.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        throw new IOException("Failed to download image, HTTP response code: " + responseCode);
      }

      try (InputStream inputStream = connection.getInputStream();
          ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        return byteArrayOutputStream.toByteArray();
      }
    } finally {
      connection.disconnect();
    }
  }


  public UtilisateurModel findOrCreateUser(UtilisateurModel userModel)
      throws MessagingException, TemplateException, IOException {
    try {
      // Try to find existing user
      UtilisateurModel user = userRepo.findByEmail(userModel.email());
      log.info("Found existing user: {}", user.email());
      userRepo.save(user);
      // Ensure auth exists
      Authentification auth = findOrCreateAuth(user);
      log.info("User authentication: {}", auth.id());
      return user;

    } catch (NotFoundException e) {
      // Create new user if not found
      ImageUploadResponse imageUploadResponse =
          saveImageToDisk(userModel.image(), userModel.email());
      log.info("Creating new user for email: {}", userModel.email());
      UtilisateurModel newUser =
          UtilisateurModel.builder().email(userModel.email()).nom(userModel.nom())
              .prenom(userModel.prenom()).imageUrl(imageUploadResponse.getFilename())
              .roles(Collections.singleton(Role.CLIENT)).build();

      UtilisateurModel savedUser = userRepo.save(newUser);

      log.info("Created new user with ID: {}", savedUser.id());

      log.info("Sending account creation event");
      metaPixelService.sendLeadEvent("Account creation via google", savedUser.email());
      // Create authentication
      Authentification auth = findOrCreateAuth(savedUser);
      log.info("Created authentication with ID: {}", auth.id());
      return savedUser;
    }
  }

  public Authentification findOrCreateAuth(UtilisateurModel user)
      throws MessagingException, TemplateException, IOException {
    try {

      // Try to find existing auth
      Authentification auth = authRepo.findByUserEmailAndProviderId(user.email(), "1");
      log.info("Found existing authentication for user: {}", user.email());

      // Update refresh token
      AuthentificationEntity entity = authMapper.modelToEntity(auth);
      entity.setRefreshToken(jwtUtil.generateRefreshToken(CustomUserDetails.builder()
          .email(user.email()).username(user.prenom() + " " + user.nom())
          .authorities(mapRolesToAuthorities(user.roles())).id(user.id()).providerId("1").build()));

      Authentification updatedAuth = authRepo.save(authMapper.entityToModel(entity));
      log.info("Updated authentication refresh token");

      return updatedAuth;
    } catch (NotFoundException e) {
      // Create new auth if not found
      log.info("Creating new authentication for user: {}", user.email());
      Authentification auth =
          Authentification.builder().user(user).providerId("1").type(TypeAuthentification.GOOGLE)
              .token(jwtUtil.generateRefreshToken(CustomUserDetails.builder().email(user.email())
                  .username(user.prenom() + " " + user.nom())
                  .authorities(mapRolesToAuthorities(user.roles())).id(user.id()).providerId("1")
                  .build()))
              .build();
      // emailInterface.sendVerificationEmail(user.email());
      Authentification savedAuth = authRepo.save(auth);
      log.info("Created new authentication with ID: {}", savedAuth.id());

      return savedAuth;
    }
  }

  private Collection<GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
    return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
        .collect(Collectors.toList());
  }

  private ImageUploadResponse saveImageToDisk(byte[] imageBytes, String email) throws IOException {
    File dir = new File(uploadDir);
    if (!dir.exists() && !dir.mkdirs()) {
      throw new IOException("Failed to create directory: " + uploadDir);
    }

    String filename = UUID.randomUUID() + "_profile-image-" + email + ".jpg";
    Path path = Paths.get(dir.getAbsolutePath(), filename); // safer path concatenation
    Files.write(path, imageBytes);
    log.info("Saved profile image for {} at {}", email, path.toAbsolutePath());
    ImageUploadResponse imageUploadResponse = new ImageUploadResponse();
    imageUploadResponse.setFilename(filename);
    return imageUploadResponse;
  }



}
