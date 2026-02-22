package tn.temporise.ti;

import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tn.temporise.domain.model.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Slf4j
class BlogPostControllerIntegrationTest extends BaseIntegrationTest {

  private Long testArticleId;

  @BeforeEach
  void setUpTestData() {
    BlogPost post = BlogPost.builder().titre("Integration Test Title")
        .contenu("Integration Test Content").status(BlogPostStatus.BROUILLON)
        .user(UtilisateurModel.builder().id(userId).build()).build();

    BlogPost savedPost = blogPostRepo.save(post);
    testArticleId = savedPost.id();
  }

  @Test
  void shouldCreateBlogPost() {
    ArticleRequest request = new ArticleRequest();
    request.setTitre("New Blog");
    request.setContenu("This is a blog post.");
    request.setStatus(ArticleRequest.StatusEnum.PUBLIER);
    request.setAuteur(userId.toString());

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .post("/v1/articles").then().statusCode(HttpStatus.CREATED.value())
        .body("titre", equalTo("New Blog")).body("contenu", equalTo("This is a blog post."));
  }

  @Test
  void shouldGetArticleById() {
    given().cookie("jwt", jwtToken).when().get("/v1/articles/" + testArticleId).then()
        .statusCode(HttpStatus.OK.value()).body("id", equalTo(testArticleId.intValue()))
        .body("titre", equalTo("Integration Test Title"));
  }

  @Test
  void shouldGetAllArticles() {
    given().cookie("jwt", jwtToken).when().get("/v1/articles").then()
        .statusCode(HttpStatus.OK.value()).body("size()", greaterThan(0));
  }

  @Test
  void shouldUpdateBlogPost() {
    ArticleRequest request = new ArticleRequest();
    request.setTitre("Updated Blog Title");
    request.setContenu("Updated content.");
    request.setStatus(ArticleRequest.StatusEnum.BROUILLON);

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .put("/v1/articles/" + testArticleId).then().statusCode(HttpStatus.OK.value())
        .body("titre", equalTo("Updated Blog Title"));
  }

  @Test
  void shouldDeleteBlogPost() {
    given().cookie("jwt", jwtToken).when().delete("/v1/articles/" + testArticleId).then()
        .statusCode(HttpStatus.OK.value()).body("code", equalTo("200"))
        .body("message", equalTo("Toutes les catégories ont été supprimés avec succès"));
  }

}
