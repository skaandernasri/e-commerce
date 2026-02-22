package tn.temporise.ti;

import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import tn.temporise.domain.model.*;

import java.time.OffsetDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Slf4j
class AvisControllerIntegrationTest extends BaseIntegrationTest {

  private Long testProductId;
  private Long testAvisId;
  private Long testUserId;

  @BeforeEach
  void setUpTestData() {
    // Create a test product
    Categorie testCategory = new Categorie(null, "Test Category", "Test Desc");
    Categorie savedCategory = categorieRepo.save(testCategory);

    Produit testProduct = Produit.builder().nom("Test Product").description("Desc").prix(100.0)
        .imageProduits(null).categorie(savedCategory).promotions(null).build();
    Produit savedProduct = productRepo.save(testProduct);
    testProductId = savedProduct.id();
    Variant testVariant = Variant.builder().produit(savedProduct).color("red").size("M")
        .quantity(10L).availableQuantity(0L).build();
    variantRepo.save(testVariant);


    // Create a test avis
    UtilisateurModel userModel = userRepo.findById(userId);
    testUserId = userModel.id();
    log.info("user in database : {}", userModel);
    Avis testAvis = Avis.builder().commentaire("Test comment").note(4).produit(savedProduct)
        .utilisateur(userModel).build();
    Avis savedAvis = avisRepo.save(testAvis);
    testAvisId = savedAvis.id();
  }

  @Test
  void shouldCreateAvis() {
    AvisRequest request = new AvisRequest();
    request.setCommentaire("Great product!");
    request.setUtilisateurId(userId);
    request.setProduitId(testProductId);
    request.setDatePublication(OffsetDateTime.now());
    request.setNote(5);

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .post("/v1/avis").then().log().all(true).statusCode(HttpStatus.CREATED.value())
        .body("commentaire", equalTo(request.getCommentaire()))
        .body("note", equalTo(request.getNote()));
  }

  @Test
  void shouldGetAvisById() {
    given().cookie("jwt", jwtToken).when().get("/v1/avis/" + testAvisId).then()
        .statusCode(HttpStatus.OK.value()).body("id", equalTo(testAvisId.intValue()))
        .body("commentaire", equalTo("Test comment")).body("note", equalTo(4));
  }

  @Test
  void shouldGetAllAvis() {
    given().cookie("jwt", jwtToken).when().get("/v1/avis").then().statusCode(HttpStatus.OK.value())
        .body("size()", greaterThan(0)).body("[0].id", equalTo(testAvisId.intValue()));
  }

  @Test
  void shouldGetAvisByProductId() {
    given().cookie("jwt", jwtToken).when().get("/v1/avisByProductId/" + testProductId).then()
        .statusCode(HttpStatus.OK.value()).body("size()", greaterThan(0))
        .body("[0].produit.id", equalTo(testProductId.intValue()));
  }

  @Test
  void shouldGetAvisByUserId() {
    given().cookie("jwt", jwtToken).when().get("/v1/avisByUserId/" + testUserId).then()
        .statusCode(HttpStatus.OK.value()).body("size()", greaterThan(0))
        .body("[0].utilisateur.id", equalTo(testUserId.intValue()));
  }



  @Test
  void shouldUpdateAvis() {
    AvisRequest request = new AvisRequest();
    request.setCommentaire("Updated comment");
    request.setNote(3);
    request.setProduitId(testProductId);
    request.setUtilisateurId(testUserId);
    request.setDatePublication(OffsetDateTime.now());

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .put("/v1/avis/" + testAvisId).then().statusCode(HttpStatus.OK.value())
        .body("id", equalTo(testAvisId.intValue())).body("commentaire", equalTo("Updated comment"))
        .body("note", equalTo(3));
  }

  @Test
  void getPaginatedAvis() {
    given().cookie("jwt", jwtToken).param("page", 0).param("size", 10)
        .param("comment", "Test comment").when().get("/v1/avis/paged/filtered").then().log()
        .all(true).statusCode(HttpStatus.OK.value()).body("content.size()", greaterThan(0));
  }

  @Test
  void paginatedAvisWrontFilter() {
    given().cookie("jwt", jwtToken).param("page", 0).param("size", 10)
        .param("comment", "no comment").when().get("/v1/avis/paged/filtered").then().log().all(true)
        .statusCode(HttpStatus.OK.value()).body("content.size()", equalTo(0));
  }

  @Test
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  void shouldDeleteAvis() {
    given().cookie("jwt", jwtToken).when().delete("/v1/avis/" + testAvisId).then()
        .statusCode(HttpStatus.OK.value()).body("code", equalTo("200"))
        .body("message", equalTo("Avis supprimé !"));
  }

  @Test
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  void shouldDeleteAllAvis() {
    given().cookie("jwt", jwtToken).when().delete("/v1/avis").then()
        .statusCode(HttpStatus.OK.value()).body("code", equalTo("200"))
        .body("message", equalTo("Tous les avis ont été supprimés avec succès"));
  }
}
