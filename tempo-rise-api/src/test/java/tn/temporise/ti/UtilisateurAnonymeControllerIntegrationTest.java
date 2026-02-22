package tn.temporise.ti;

import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tn.temporise.domain.model.UtilisateurAnonymeRequest;

import java.util.UUID;

import static io.restassured.RestAssured.given;

@Slf4j
class UtilisateurAnonymeControllerIntegrationTest extends BaseIntegrationTest {

  private UUID testSessionToken;

  @BeforeEach
  void setUpTestData() {
    testSessionToken = UUID.randomUUID();
  }

  @Test
  void shouldCreateOrGetAnonymousUser() {
    UtilisateurAnonymeRequest request = new UtilisateurAnonymeRequest();
    request.setSessionToken(testSessionToken);

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .post("/v1/utilisateur/anonyme").then().log().all(true).statusCode(HttpStatus.OK.value());
  }



}
