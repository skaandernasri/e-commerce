package tn.temporise.ti;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tn.temporise.application.service.RecaptchaService;
import tn.temporise.domain.model.*;


import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

@Slf4j
public class AuthIntegrationTest extends BaseIntegrationTest {
  @MockitoBean
  private RecaptchaService recaptchaService;
  RecaptchaResponse mockResponse;

  @BeforeEach
  void setUpTestData() {
    super.setUp();
    doNothing().when(recaptchaService).verifyToken(anyString());
    UtilisateurModel user = UtilisateurModel.builder().nom("TEST").email("signin@example.com")
        .password("Password123.").roles(Set.of(Role.CLIENT)).isverified(true).build();
    UtilisateurModel savedUser = userRepo.save(user); // Save and get managed entity
    Authentification auth =
        Authentification.builder().type(TypeAuthentification.EMAIL).providerId("0").user(savedUser)
            .token(null).password(passwordEncoder.encode(user.password())).build();
    authRepo.save(auth);
  }

  @Test
  @Order(1)
  void testSignupUser_Success() {
    SignupUserRequest signupUserRequest = new SignupUserRequest();
    signupUserRequest.setNom("skander");
    signupUserRequest.setPassword("skandeR123.");
    signupUserRequest.setEmail("test123@example.com");
    signupUserRequest.setRoles(List.of("CLIENT"));
    signupUserRequest.setIsverified(false);
    signupUserRequest.setReCaptchaToken("captcha_token_example");

    Response response = given().contentType(ContentType.JSON).body(signupUserRequest).when()
        .post("/v1/auth/signup").then().log().all().extract().as(Response.class);

    assertNotNull(response.getMessage());
    log.info("Response: {}", response);
    assertEquals("201", response.getCode());
    UtilisateurModel user = userRepo.findByEmail("test@example.com");
    assertNotNull(user);
    assertEquals("test@example.com", user.email());
  }

  @Test
  @Order(2)
  void testSignupUser_Failed() {
    SignupUserRequest signupUserRequest = new SignupUserRequest();
    signupUserRequest.setNom("skander");
    signupUserRequest.setPassword("skandeR123.");
    signupUserRequest.setEmail("test@example.com");
    signupUserRequest.setRoles(List.of("CLIENT"));
    signupUserRequest.setIsverified(false);
    signupUserRequest.setReCaptchaToken("captcha_token_example");

    given().contentType(ContentType.JSON).body(signupUserRequest).when().post("/v1/auth/signup")
        .then().log().all().statusCode(409);
    UtilisateurModel user = userRepo.findByEmail("test@example.com");
    assertNotNull(user);
  }

  @Test
  @Order(3)
  void testSigninUser_Success() {
    SigninUserRequest signinUserRequest =
        new SigninUserRequest("signin@example.com", "Password123.", "captcha_token_example");
    Response response = RestAssured.given().contentType("application/json").body(signinUserRequest)
        .when().post("/v1/auth/signin").then().log().all(true).statusCode(200)
        .contentType(ContentType.JSON).extract().as(Response.class);


    assertNotNull(response.getMessage());
    assertEquals("200", response.getCode());
    assertNotNull(response.getDetails());
  }

  @Test
  @Order(4)
  void testLogoutUser_Success() {
    assertNotNull(jwtToken, "JWT token must be set from the signin test!");

    Response response = RestAssured.given().contentType("application/json").cookie("jwt", jwtToken)
        .when().post("/v1/auth/logout").then().extract().as(Response.class);
    assertNotNull(response);

  }
}
