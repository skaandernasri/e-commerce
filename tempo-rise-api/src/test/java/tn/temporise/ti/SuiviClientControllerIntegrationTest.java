package tn.temporise.ti;

import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tn.temporise.domain.model.*;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


@Slf4j
class SuiviClientControllerIntegrationTest extends BaseIntegrationTest {

  private Long testProductId;
  private Long testUserId;
  private UUID testAnonymousUserToken;

  @BeforeEach
  void setUpTestData() {
    // Create a test product
    Categorie testCategory = new Categorie(null, "Test Category", "Test Desc");
    Categorie savedCategory = categorieRepo.save(testCategory);

    Produit testProduct = Produit.builder().nom("Test Product").description("Desc").prix(100.0)
        .categorie(savedCategory).build();
    Produit savedProduct = productRepo.save(testProduct);
    testProductId = savedProduct.id();

    // Get test user
    UtilisateurModel userModel = userRepo.findById(userId);
    testUserId = userModel.id();

    // Create anonymous user token
    testAnonymousUserToken = UUID.randomUUID();
    UtilisateurAnonyme anonymousUser =
        UtilisateurAnonyme.builder().sessionToken(testAnonymousUserToken)
            .email(testAnonymousUserToken + "@temposphere.tn").build();
    utilisateurAnonymeRepo.save(anonymousUser);
  }

  @Test
  void shouldTrackActionForRegisteredUser() {
    SuiviClientRequest request = new SuiviClientRequest();
    request.setProduitId(testProductId);
    request.setUtilisateurId(testUserId);
    request.setTypeAction(SuiviClientRequest.TypeActionEnum.VIEW_PRODUCT);

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .post("/v1/suiviClient").then().statusCode(HttpStatus.OK.value())
        .body("code", equalTo("201")).body("message", equalTo("Suivi client saved or updated"));
  }

  @Test
  void shouldTrackActionForAnonymousUser() {
    SuiviClientRequest request = new SuiviClientRequest();
    request.setProduitId(testProductId);
    request.setUtilisateurAnonymeUuid(testAnonymousUserToken);
    request.setTypeAction(SuiviClientRequest.TypeActionEnum.VIEW_PRODUCT);

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .post("/v1/suiviClient").then().statusCode(HttpStatus.OK.value())
        .body("code", equalTo("201")).body("message", equalTo("Suivi client saved or updated"));
  }

  @Test
  void shouldMergeAnonymousUserActions() {
    // First create some anonymous actions
    SuiviClientRequest trackRequest = new SuiviClientRequest();
    trackRequest.setProduitId(testProductId);
    trackRequest.setUtilisateurAnonymeUuid(testAnonymousUserToken);
    trackRequest.setTypeAction(SuiviClientRequest.TypeActionEnum.VIEW_PRODUCT);
    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(trackRequest)
        .post("/v1/suiviClient");

    // Now test merging
    SuiviClientRequest mergeRequest = new SuiviClientRequest();
    mergeRequest.setUtilisateurId(testUserId);
    mergeRequest.setUtilisateurAnonymeUuid(testAnonymousUserToken);

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(mergeRequest).when()
        .post("/v1/suiviClient/merge").then().statusCode(HttpStatus.OK.value())
        .body("code", equalTo("201")).body("message", equalTo("Actions merged successfully"));
  }

  @Test
  void shouldReturnBadRequestForInvalidTrackingRequest() {
    SuiviClientRequest request = new SuiviClientRequest(); // Missing required fields

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .post("/v1/suiviClient").then().statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void shouldReturnNotFoundForInvalidProduct() {
    SuiviClientRequest request = new SuiviClientRequest();
    request.setProduitId(9999L); // Invalid product ID
    request.setUtilisateurId(testUserId);
    request.setTypeAction(SuiviClientRequest.TypeActionEnum.VIEW_PRODUCT);

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .post("/v1/suiviClient").then().statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void shouldReturnNotFoundForInvalidUserInMerge() {
    SuiviClientRequest request = new SuiviClientRequest();
    request.setUtilisateurId(9999L); // Invalid user ID
    request.setUtilisateurAnonymeUuid(testAnonymousUserToken);

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .post("/v1/suiviClient/merge").then().statusCode(HttpStatus.NOT_FOUND.value());
  }
}
