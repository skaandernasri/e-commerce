package tn.temporise.ti;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tn.temporise.domain.model.Categorie;
import tn.temporise.domain.model.CategorieRequest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class CategorieControllerIntegrationTest extends BaseIntegrationTest {

  private Long testCategoryId;

  @BeforeEach
  void setUpTestData() {
    // Create a test category that will be used in the tests
    Categorie testCategory = new Categorie(null, "Test Category", "Test Description");
    Categorie savedCategory = categorieRepo.save(testCategory);
    testCategoryId = savedCategory.id();
  }

  @Test
  void shouldGetAllCategories() {
    given().cookie("jwt", jwtToken).when().get("/v1/categories").then()
        .statusCode(HttpStatus.OK.value()).body("$", hasSize(greaterThan(0)));
  }

  @Test
  void shouldGetCategoryById() {
    given().cookie("jwt", jwtToken).when().get("/v1/categories/" + testCategoryId).then()
        .statusCode(HttpStatus.OK.value()).body("id", equalTo(testCategoryId.intValue()));
  }

  @Test
  void shouldCreateCategory() {
    CategorieRequest request = new CategorieRequest();
    request.setNom("New Test Category");
    request.setDescription("New Test Description");

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .post("/v1/categories").then().statusCode(HttpStatus.CREATED.value())
        .body("nom", equalTo(request.getNom()));
  }

  @Test
  void shouldUpdateCategory() {
    CategorieRequest request = new CategorieRequest();
    request.setNom("Updated Category");
    request.setDescription("Updated Description");

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .put("/v1/categories/" + testCategoryId).then().statusCode(HttpStatus.OK.value())
        .body("nom", equalTo(request.getNom()));
  }
}
