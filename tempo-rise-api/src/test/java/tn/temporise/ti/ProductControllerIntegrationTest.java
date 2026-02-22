package tn.temporise.ti;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tn.temporise.domain.model.*;
import tn.temporise.domain.model.Categorie;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class ProductControllerIntegrationTest extends BaseIntegrationTest {

  private Long testCategoryId;
  private Long testProductId;
  private Long testVariantId;

  @BeforeEach
  void setUpTestData() {
    // Create a test category
    Categorie testCategory = new Categorie(null, "Test Category", "Test Description");
    Categorie savedCategory = categorieRepo.save(testCategory);
    testCategoryId = savedCategory.id();
    Produit testProduct = Produit.builder().nom("Test Product").description("Description")
        .prix(99.99).imageProduits(null).categorie(savedCategory).promotions(null).build();
    Produit savedProduct = productRepo.save(testProduct);
    testProductId = savedProduct.id();
    ImageProduit testImage = ImageProduit.builder().url("test.jpg").produit(savedProduct).build();
    imageProduitRepo.save(testImage);
    Variant testVariant = Variant.builder().color("red").size("M").produit(savedProduct)
        .quantity(10L).availableQuantity(0L).build();
    Variant savedVariant = variantRepo.save(testVariant);
    testVariantId = savedVariant.id();
  }

  @Test
  void shouldCreateProduct() {
    VariantRequest variantRequest = new VariantRequest();
    variantRequest.setColor("Red");
    variantRequest.setSize("M");
    variantRequest.setQuantity(10L);
    ProductRequest request = new ProductRequest();
    request.setNom("New Product");
    request.setDescription("Description");
    request.setPrix(99.99);
    request.setVariants(List.of(variantRequest));
    request.setCategorie(testCategoryId);

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .post("/v1/produits").then().statusCode(HttpStatus.CREATED.value())
        .body("nom", equalTo(request.getNom()));
  }

  @Test
  void shouldGetProductById() {
    given().cookie("jwt", jwtToken).when().get("/v1/produits/" + testProductId).then()
        .statusCode(HttpStatus.OK.value()).body("id", equalTo(testProductId.intValue()));
  }

  @Test
  void shouldGetAllProducts() {
    given().cookie("jwt", jwtToken).when().get("/v1/produits").then()
        .statusCode(HttpStatus.OK.value()).body("size()", greaterThan(0));
  }

  @Test
  void shouldUpdateProductById() {
    VariantRequest variantRequest = new VariantRequest();
    variantRequest.setColor("Red");
    variantRequest.setSize("M");
    variantRequest.setQuantity(10L);
    ProductRequest request = new ProductRequest();
    request.setNom("Updated Product");
    request.setDescription("Description");
    request.setPrix(99.99);
    request.setMarque("Test Marque");
    request.setVariants(List.of(variantRequest));
    request.setCategorie(testCategoryId);

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .put("/v1/produits/" + testProductId).then().statusCode(HttpStatus.OK.value()).log()
        .all(true).body("nom", equalTo(request.getNom()));
  }

  @Test
  void getPaginatedProducts() {
    given().cookie("jwt", jwtToken).param("page", 0).param("size", 10)
        .param("productName", "Test Product").when().get("/v1/produits/paged/filtered").then().log()
        .all(true).statusCode(HttpStatus.OK.value()).body("content.size()", greaterThan(0));
  }

  @Test
  void paginatedProductsWrontFilter() {
    given().cookie("jwt", jwtToken).param("page", 0).param("size", 10)
        .param("productName", "Wrong name").when().get("/v1/produits/paged/filtered").then().log()
        .all(true).statusCode(HttpStatus.OK.value()).body("content.size()", equalTo(0));
  }

  @Test
  void shouldDeleteProductById() {
    given().cookie("jwt", jwtToken).when().delete("/v1/produits/" + testProductId).then().log()
        .all(true).statusCode(HttpStatus.OK.value());
  }

}
