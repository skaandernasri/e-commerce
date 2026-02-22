package tn.temporise.ti;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tn.temporise.domain.model.*;
import tn.temporise.domain.model.Categorie;
import tn.temporise.domain.model.Produit;


import java.io.File;
import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class ImageProdControllerIntegrationTest extends BaseIntegrationTest {

  private Long testProductId;
  private Long testImageId;

  @BeforeEach
  void setUpTestData() {
    // Create test product
    Categorie testCategory = new Categorie(null, "Test Category", "Test Desc");
    Categorie savedCategory = categorieRepo.save(testCategory);

    Produit testProduct = Produit.builder().nom("Test Product").description("Desc").prix(100.0)
        .imageProduits(null).categorie(savedCategory).promotions(null).build();
    Produit savedProduct = productRepo.save(testProduct);
    Variant testVariant = Variant.builder().color("red").size("M").produit(savedProduct)
        .quantity(10L).availableQuantity(0L).build();
    variantRepo.save(testVariant);
    testProductId = savedProduct.id();

    // Create test image
    ImageProduit testImage =
        ImageProduit.builder().contenu(new byte[0]).produit(savedProduct).build();
    ImageProduit savedImage = imageProduitRepo.save(testImage);
    testImageId = savedImage.id();
  }

  @Test
  void shouldCreateImage() throws IOException {
    File imageFile = new File("src/test/resources/image.png");
    if (!imageFile.exists()) {
      throw new RuntimeException("Test image not found: " + imageFile.getAbsolutePath());
    }
    given().cookie("jwt", jwtToken).multiPart("contenu", imageFile, "image/png") // sending the file
        .multiPart("produitId", testProductId.toString()) // sending produitId as a field
        .when().post("/v1/imageProduit").then().log().all(true)
        .statusCode(HttpStatus.CREATED.value()).body("id", notNullValue()); // assuming the response
                                                                            // contains the uploaded
                                                                            // image URL
  }


  @Test
  void shouldGetImageById() {
    given().cookie("jwt", jwtToken).when().get("/v1/imageProduit/" + testImageId).then().log()
        .all(true).statusCode(HttpStatus.OK.value()).contentType("image/png"); // ou autre selon ton
                                                                               // endpoint
  }

}
