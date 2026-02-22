package tn.temporise.ti;

import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tn.temporise.domain.model.*;


import java.io.File;
import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Slf4j
class UserControllerIntegrationTest extends BaseIntegrationTest {
  private Long testUserId;
  private final String testUserEmail = "test.user@example.com";

  @BeforeEach
  void setUpTestData() {
    UtilisateurModel user = UtilisateurModel.builder().nom("TEST").email(testUserEmail)
        .password("Password123").roles(Set.of(Role.CLIENT)).isverified(true).build();
    testUserId = userRepo.save(user).id();
    log.info("testUserId in beforeEach clause : {}", testUserId);
  }

  @Order(1)
  @Test
  void shouldCreateUser() {
    UserRequest request = new UserRequest();
    request.setEmail("new.user@example.com");
    request.setPassword("Password123");
    request.setNom("New");
    request.setPrenom("New");
    // request.set("ImageUrl");
    request.setTelephone("5555555555");
    request.setRoles(List.of("CLIENT"));
    request.isverified(true);

    given().cookie("jwt", jwtToken) // Admin token required
        .contentType(ContentType.JSON).body(request).when().post("/v1/utilisateurs").then().log()
        .all(true).statusCode(HttpStatus.CREATED.value()).body("email", equalTo(request.getEmail()))
        .body("nom", equalTo(request.getNom()));
  }

  @Order(2)
  @Test
  void shouldGetUserById() {
    given().cookie("jwt", jwtToken) // Admin token required
        .when().get("/v1/utilisateurs/" + userId).then().log().all(true)
        .statusCode(HttpStatus.OK.value()).body("id", equalTo(userId.intValue()));

  }

  @Order(3)
  @Test
  void shouldGetAllUsers() {
    given().cookie("jwt", jwtToken) // Admin token required
        .when().get("/v1/utilisateurs").then().log().all(true).statusCode(HttpStatus.OK.value())
        .body("size()", greaterThan(0));
  }

  @Order(4)
  @Test
  void shouldUpdateUser() {
    UserRequest request = new UserRequest();
    request.setEmail("updated.user@example.com");
    request.password("UpdatedPassword123");
    request.setNom("Updated");
    request.setPrenom("Updated");
    // request.setImageUrl("UpdatedImageUrl");
    request.setTelephone("5555555555");
    request.setRoles(List.of("GESTIONNAIRE"));
    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .put("/v1/utilisateurs/" + testUserId).then().log().all(true)
        .statusCode(HttpStatus.OK.value()).body("nom", equalTo("Updated"))
        .body("email", equalTo("updated.user@example.com"));
  }

  @Order(5)
  @Test
  void shouldUpdateProfileImageUrl() {
    // UpdateImageProfilUtilisateurRequest request = new UpdateImageProfilUtilisateurRequest();
    // request.setImageUrl("UpdatedImageUrl");
    File imageFile = new File("src/test/resources/image.png"); // path to your test image
    if (!imageFile.exists()) {
      throw new RuntimeException("Test image not found: " + imageFile.getAbsolutePath());
    }
    given().cookie("jwt", jwtToken).multiPart("image", imageFile, "image/png").when()
        .post("/v1/utilisateur/" + testUserId + "/profile/image").then().log().all(true)
        .statusCode(HttpStatus.OK.value());
  }

  @Order(6)
  @Test
  void shouldUpdateProfile() {
    UserRequest request = new UserRequest();
    request.setId(testUserId);
    request.setEmail("updated.user@example.com");
    request.password("UpdatedPassword123");
    request.setNom("Updated");
    request.setPrenom("Updated");
    // request.setImageUrl("UpdatedImageUrl");
    request.setTelephone("5555555555");
    request.setRoles(List.of("GESTIONNAIRE"));

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .put("/v1/utilisateur/profile").then().log().all(true).statusCode(HttpStatus.OK.value());
  }

  @Order(7)
  @Test
  void shouldDeleteUser() {
    given().cookie("jwt", jwtToken).when().delete("/v1/utilisateurs/" + testUserId).then().log()
        .all(true).statusCode(HttpStatus.OK.value()).body("code", equalTo("200"));
    // .body("message", equalTo("User deleted successfully"));

    // Verify deletion
  }



}
