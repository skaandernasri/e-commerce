package tn.temporise.ti;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tn.temporise.domain.model.CodePromo;
import tn.temporise.domain.model.CodePromoRequest;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CodePromoControllerIntegrationTest extends BaseIntegrationTest {



  private Long testCodePromoId;

  @BeforeEach
  void setUpTestData() {

    CodePromo testCodePromo = CodePromo.builder().code("TESTCODE").reduction(10.0)
        .dateExpiration(LocalDateTime.now(ZoneOffset.UTC).plusSeconds(86400)) // Tomorrow
        .build();
    CodePromo saved = codePromoRepo.save(testCodePromo);
    testCodePromoId = saved.id();
  }

  @Test
  void createCodePromo_Success() {
    CodePromoRequest request =
        new CodePromoRequest(OffsetDateTime.now().plusDays(2), "NEWCODE", 15.0); // 2 days from now

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .post("/v1/codePromo").then().statusCode(HttpStatus.CREATED.value())
        .body("code", equalTo("NEWCODE")).body("reduction", equalTo(15.0F));
  }

  @Test
  void getCodePromoById_Success() {
    given().cookie("jwt", jwtToken).when().get("/v1/codePromo/" + testCodePromoId).then()
        .statusCode(HttpStatus.OK.value()).body("code", equalTo("TESTCODE"))
        .body("reduction", equalTo(10.0F));
  }

  @Test
  void getAllCodePromos_Success() {
    given().cookie("jwt", jwtToken).when().get("/v1/codePromo").then()
        .statusCode(HttpStatus.OK.value()).body("$", hasSize(greaterThanOrEqualTo(1)));
  }

  @Test
  void getActiveCodePromos_Success() {
    given().cookie("jwt", jwtToken).when().get("/v1/codePromo/active").then()
        .statusCode(HttpStatus.OK.value()).body("$", hasSize(greaterThanOrEqualTo(1)))
        .body("[0].code", equalTo("TESTCODE"));
  }

  @Test
  void updateCodePromo_Success() {
    CodePromoRequest request =
        new CodePromoRequest(OffsetDateTime.now().plusDays(3), "UPDATED", 20.0);

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .put("/v1/codePromo/" + testCodePromoId).then().statusCode(HttpStatus.OK.value())
        .body("code", equalTo("UPDATED")).body("reduction", equalTo(20.0F));
  }

  @Test
  void deleteCodePromo_Success() {
    given().cookie("jwt", jwtToken).when().delete("/v1/codePromo" + "/" + testCodePromoId).then()
        .statusCode(HttpStatus.OK.value())
        .body("message", equalTo("Code promo supprimé avec succès"));
  }
}
