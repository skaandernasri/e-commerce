package tn.temporise.ti;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.parsing.Parser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import tn.temporise.application.mapper.AuthMapper;
import tn.temporise.application.mapper.RegMapper;
import tn.temporise.application.service.CustomUserDetailsService;
import tn.temporise.application.service.NotifService;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.*;
import tn.temporise.infrastructure.persistence.entity.AuthentificationEntity;
import tn.temporise.infrastructure.persistence.entity.UtilisateurEntity;
import tn.temporise.infrastructure.security.utils.CookiesUtil;
import tn.temporise.infrastructure.security.utils.JwtUtil;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Testcontainers
@TestPropertySource(properties = {"spring.profiles.active=test"})
@Slf4j
public abstract class BaseIntegrationTest {

  @LocalServerPort
  protected Integer port;
  @Autowired
  protected NotifService notifService;
  @Autowired
  protected UserNotifRepo userNotifRepo;
  @Autowired
  protected CookiesUtil cookiesUtil;
  @Autowired
  protected BlogPostRepo blogPostRepo;
  @Autowired
  protected VariantRepo variantRepo;
  @Autowired
  protected UtilisateurAnonymeRepo utilisateurAnonymeRepo;
  @Autowired
  protected SuiviClientRepo suiviClientRepo;
  @Autowired
  protected FactureRepo factureRepo;
  @Autowired
  protected PromotionRepo promotionRepo;
  @Autowired
  protected AdresseRepo adresseRepo;
  @Autowired
  protected CommandeRepo commandeRepo;
  @Autowired
  protected CategorieRepo categorieRepo;
  @Autowired
  protected PanierVariantRepo panierVariantRepo;
  @Autowired
  protected PanierRepo panierRepo;
  @Autowired
  protected ProductRepo productRepo;
  @Autowired
  protected SectionRepo sectionRepo;
  @Autowired
  protected LigneCommandeRepo ligneCommandeRepo;
  @Autowired
  protected AvisRepo avisRepo;
  @Autowired
  protected ImageProduitRepo imageProduitRepo;
  @Autowired
  protected JwtUtil jwtUtil;
  @Autowired
  protected UserRepo userRepo;
  @Autowired
  protected CodePromoRepo codePromoRepo;
  @Autowired
  protected AuthRepo authRepo;
  @Autowired
  protected RegMapper regMapper;
  @Autowired
  protected AuthMapper authMapper;
  @Autowired
  protected CustomUserDetailsService customUserDetailsService;
  @Autowired
  protected PasswordEncoder passwordEncoder;

  protected Long userId;
  protected String jwtToken;
  protected UtilisateurModel currentUser;
  public static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:15")
      .withDatabaseName("test-db").withUsername("test").withPassword("test");
  static {
    container.start();
  }

  @DynamicPropertySource
  static void datasourceConfig(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", container::getJdbcUrl);
    registry.add("spring.datasource.username", container::getUsername);
    registry.add("spring.datasource.password", container::getPassword);
  }


  @BeforeAll
  static void setupRestAssured() {
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
        new ObjectMapperConfig().jackson2ObjectMapperFactory((cls, charset) -> objectMapper));

    RestAssured.defaultParser = Parser.JSON;
  }

  @BeforeEach
  void setUp() {
    RestAssured.baseURI = "http://localhost:" + port;

    // Clean database
    factureRepo.deleteAll();
    commandeRepo.deleteAll();
    adresseRepo.deleteAll();
    codePromoRepo.deleteAll();
    authRepo.deleteAll();
    userRepo.deleteAll();
    avisRepo.deleteAll();
    productRepo.deleteAll();
    categorieRepo.deleteAll();
    panierRepo.deleteAll();
    panierVariantRepo.deleteAll();
    promotionRepo.deleteAll();

    // Create user
    UtilisateurEntity user = createUser("test@example.com", Role.ADMIN, true);
    currentUser = userRepo.save(regMapper.entityToModel(user));
    this.userId = currentUser.id();

    AuthentificationEntity auth = createAuthentication(currentUser, "Password123");
    authRepo.save(authMapper.entityToModel(auth));

    CustomUserDetails userDetails = setupSecurityContext(user);
    this.jwtToken = jwtUtil.generateAccessToken(userDetails);

    assertNotNull(jwtToken);
  }

  private UtilisateurEntity createUser(String email, Role role, boolean isVerified) {
    return new UtilisateurEntity(email, role, isVerified);
  }

  private AuthentificationEntity createAuthentication(UtilisateurModel userModel, String password) {
    UtilisateurEntity userEntity = regMapper.modelToEntity(userModel);
    AuthentificationEntity auth = new AuthentificationEntity();
    auth.setUser(userEntity);
    auth.setPassword(passwordEncoder.encode(password));
    auth.setType(TypeAuthentification.EMAIL);
    auth.setProviderId("0");
    return auth;
  }

  private CustomUserDetails setupSecurityContext(UtilisateurEntity user) {
    CustomUserDetails userDetails =
        customUserDetailsService.loadUserByUsername(user.getEmail() + ";" + "0");
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
    return userDetails;
  }

  @Test
  void contextLoads() {
    assertNotNull(jwtUtil, "JwtUtil should be injected");
    assertNotNull(userRepo, "UserRepo should be injected");
    assertTrue(container.isRunning(), "Postgres container should be running");
  }
}
