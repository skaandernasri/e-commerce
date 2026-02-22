package tn.temporise.ti;

import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tn.temporise.domain.model.AdresseRequest;
import tn.temporise.domain.model.AdresseResponse;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Slf4j
class AdresseControllerIntegrationTest extends BaseIntegrationTest {

  private Long testAdresseId;

  @BeforeEach
  void setUpTestData() {
    // Create a test address
    AdresseRequest testAdresse = new AdresseRequest();
    testAdresse.setCodePostal("1000");
    testAdresse.setVille("Tunis");
    testAdresse.setPays("Tunisia");
    testAdresse.setType("LIVRAISON");
    testAdresse.setUtilisateurId(userId);
    testAdresse.setLigne1("ligne1");

    AdresseResponse createdAdresse = given().cookie("jwt", jwtToken).contentType(ContentType.JSON)
        .body(testAdresse).when().post("/v1/adresses").then().statusCode(HttpStatus.CREATED.value())
        .extract().as(AdresseResponse.class);

    testAdresseId = createdAdresse.getId();
  }

  @Test
  void shouldCreateAdresse() {
    AdresseRequest request = new AdresseRequest();
    request.setCodePostal("2000");
    request.setVille("Sousse");
    request.setPays("Tunisia");
    request.setType("FACTURATION");
    request.setLigne1("ligne1");
    request.setUtilisateurId(userId);

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .post("/v1/adresses").then().log().all(true).statusCode(HttpStatus.CREATED.value())
        .body("codePostal", equalTo(request.getCodePostal()))
        .body("ville", equalTo(request.getVille()));
  }

  @Test
  void shouldGetAdresseById() {
    given().cookie("jwt", jwtToken).when().get("/v1/adresses/" + testAdresseId).then()
        .statusCode(HttpStatus.OK.value()).body("id", equalTo(testAdresseId.intValue()));
  }

  @Test
  void shouldGetAllAdresses() {
    given().cookie("jwt", jwtToken).when().get("/v1/adresses").then()
        .statusCode(HttpStatus.OK.value()).body("size()", greaterThan(0));
  }

  @Test
  void shouldGetAdressesByType() {
    given().cookie("jwt", jwtToken).when().get("/v1/adresses/type/LIVRAISON").then()
        .statusCode(HttpStatus.OK.value()).body("size()", greaterThan(0));
  }

  @Test
  void shouldGetAdressesByUtilisateurId() {
    given().cookie("jwt", jwtToken).param("user_id", userId).when().get("/v1/adresses/utilisateur")
        .then().statusCode(HttpStatus.OK.value()).body("size()", greaterThan(0));
  }

  @Test
  void shouldUpdateAdresse() {
    AdresseRequest updateRequest = new AdresseRequest();
    updateRequest.setCodePostal("3000");
    updateRequest.setVille("Nabeul");
    updateRequest.setPays("Tunisia");
    updateRequest.setType("LIVRAISON");
    updateRequest.setLigne1("updatedLigne1");
    updateRequest.setUtilisateurId(userId);

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(updateRequest).when()
        .put("/v1/adresses/" + testAdresseId).then().statusCode(HttpStatus.OK.value())
        .body("codePostal", equalTo(updateRequest.getCodePostal()));
  }

  @Test
  void shouldDeleteAdresse() {
    given().cookie("jwt", jwtToken).when().delete("/v1/adresses/" + testAdresseId).then()
        .statusCode(HttpStatus.OK.value()).body("code", equalTo("200"))
        .body("message", equalTo("Adresse supprimé avec succès !"));
  }

  @Test
  void shouldDeleteAllAdresses() {
    given().cookie("jwt", jwtToken).when().delete("/v1/adresses").then()
        .statusCode(HttpStatus.OK.value()).body("code", equalTo("200"))
        .body("message", equalTo("Toutes les adresses supprimées avec succès !"));
  }
}
