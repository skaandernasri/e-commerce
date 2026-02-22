package tn.temporise.ti;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tn.temporise.domain.model.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class PromotionControllerIntegrationTest extends BaseIntegrationTest {
  private Long testProductId;
  private Long testProductId1;
  private Long testPromotionId;
  private LocalDateTime futureDate;
  private LocalDateTime pastDate;

  @BeforeEach
  void setUpTestData() {
    futureDate = LocalDateTime.now(ZoneOffset.UTC).plusDays(7);
    pastDate = LocalDateTime.now(ZoneOffset.UTC).minusDays(7);

    // Create a test product
    Categorie testCategory = new Categorie(null, "Test Category", "Test Desc");
    Categorie savedCategory = categorieRepo.save(testCategory);
    Produit testProduct = Produit.builder().nom("Test Product").description("Desc").prix(100.0)
        .categorie(savedCategory).build();
    Produit savedProduct = productRepo.save(testProduct);
    testProductId = savedProduct.id();
    Produit testProduct1 = Produit.builder().nom("Test Product").description("Desc").prix(100.0)
        .categorie(savedCategory).build();
    Produit savedProduct1 = productRepo.save(testProduct1);
    testProductId1 = savedProduct1.id();
    // Create active promotion (current date between start and end)
    Promotion activePromotion = Promotion.builder().nom("Active promotion")
        .description("Active description").produit(savedProduct).dateDebut(pastDate)
        .dateFin(futureDate).reduction(20.0).type(PromotionType.PERCENTAGE).build();
    Promotion savedActivePromotion = promotionRepo.save(activePromotion);
    testPromotionId = savedActivePromotion.id();

    // Create inactive promotion (already ended)
    Promotion inactivePromotion = Promotion.builder().nom("Inactive promotion")
        .description("Inactive description").produit(savedProduct).dateDebut(pastDate)
        .dateFin(pastDate).reduction(15.0).type(PromotionType.PERCENTAGE).build();
    promotionRepo.save(inactivePromotion);
  }

  @Test
  void shouldGetAllPromotions() {
    given().cookie("jwt", jwtToken).when().get("/v1/promotions").then()
        .statusCode(HttpStatus.OK.value()).body("$", hasSize(greaterThanOrEqualTo(1)));
  }

  @Test
  void shouldCreatePromotion() {
    PromotionRequest request = new PromotionRequest();
    request.setProduitId(testProductId1);
    request.reduction(20.0);
    request.setDateDebut(OffsetDateTime.now());
    request.setDateFin(OffsetDateTime.now().plusDays(7));
    request.setNom("Test Promotion");
    request.setType(PromotionRequest.TypeEnum.PERCENTAGE);
    request.setDescription("Test Description");

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .post("/v1/promotions").then().log().all(true).statusCode(HttpStatus.CREATED.value())
        .body("reduction", equalTo(20.0F));
  }

  @Test
  void shouldGetPromotionsByProductId() {
    given().cookie("jwt", jwtToken).when().get("/v1/promotionsByProductId/" + testProductId).then()
        .statusCode(HttpStatus.OK.value()).body("$", hasSize(greaterThanOrEqualTo(1)));
  }

  @Test
  void shouldGetActivePromotions() {
    given().cookie("jwt", jwtToken).when().get("/v1/promotions/active").then()
        .statusCode(HttpStatus.OK.value()).body("$", hasSize(greaterThanOrEqualTo(1)))
        .body("[0].nom", equalTo("Active promotion"));
  }

  @Test
  void shouldGetInactivePromotions() {
    given().cookie("jwt", jwtToken).when().get("/v1/promotions/inactive").then()
        .statusCode(HttpStatus.OK.value()).body("$", hasSize(greaterThanOrEqualTo(1)))
        .body("[0].nom", equalTo("Inactive promotion"));
  }

  @Test
  void shouldGetPromotionById() {
    given().cookie("jwt", jwtToken).when().get("/v1/promotions/" + testPromotionId).then()
        .statusCode(HttpStatus.OK.value()).body("nom", equalTo("Active promotion"))
        .body("reduction", equalTo(20.0F));
  }

  @Test
  void shouldDeletePromotion() {
    given().cookie("jwt", jwtToken).when().delete("/v1/promotions/" + testPromotionId).then()
        .statusCode(HttpStatus.OK.value()).body("code", equalTo("200"))
        .body("message", equalTo("Promotion supprimée !"));
  }
}
