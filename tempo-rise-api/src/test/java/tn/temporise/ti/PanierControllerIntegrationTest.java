package tn.temporise.ti;

import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import tn.temporise.domain.model.*;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Slf4j
class PanierControllerIntegrationTest extends BaseIntegrationTest {

  private Long testProductId;
  private Long testPanierId;
  private Long testVariantId;

  @BeforeEach
  void setUpTestData() {
    // Create test category
    Categorie testCategory = new Categorie(null, "Test Category", "Test Desc");
    Categorie savedCategory = categorieRepo.save(testCategory);

    // Create test product
    Produit testProduct = Produit.builder().nom("Test Product").description("Desc").prix(100.0)
        .imageProduits(null).categorie(savedCategory).promotions(null).build();
    Produit savedProduct = productRepo.save(testProduct);
    testProductId = savedProduct.id();
    Variant testVariant = Variant.builder().color("red").size("M").produit(savedProduct)
        .quantity(10L).availableQuantity(0L).build();
    Variant savedVariant = variantRepo.save(testVariant);
    testVariantId = savedVariant.id();
    // Create test cart
    Panier testPanier = Panier.builder().utilisateur(userRepo.findById(userId)).build();
    Panier savedPanier = panierRepo.save(testPanier);
    testPanierId = savedPanier.id();
  }

  @Test
  void shouldGetOrCreateCart() {
    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(userId).when()
        .get("/v1/panier/utilisateur/{userId}", userId).then().log().all(true)
        .statusCode(HttpStatus.OK.value());
  }

  @Test
  void shouldAddItemToPanier() {
    CartItemRequest item = new CartItemRequest();
    item.setId(testProductId);
    item.setQuantite(1L);

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(item).when()
        .post("/v1/utilisateur/{userId}/panier/add-item/{productId}", userId, testVariantId).then()
        .log().all(true).statusCode(HttpStatus.CREATED.value())

        .body("articles[0].id", equalTo(testProductId.intValue()));
  }

  @Test
  void shouldFailToAddItemWhenQuantityInvalid() {
    CartItemRequest item = new CartItemRequest();
    item.setId(testProductId);
    item.setQuantite(0L);

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(item).when()
        .post("/v1/utilisateur/{userId}/panier/add-item/{productId}", userId, testVariantId).then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void shouldGetPanierById() {
    given().cookie("jwt", jwtToken).when().get("/v1/paniers/" + testPanierId).then()
        .statusCode(HttpStatus.OK.value()).body("id", equalTo(testPanierId.intValue()));
  }

  @Test
  void shouldGetPanierByUserId() {
    given().cookie("jwt", jwtToken).when().get("/v1/panier/utilisateur/" + userId).then()
        .statusCode(HttpStatus.OK.value()).body("utilisateurId", equalTo(userId.intValue()));
  }

  @Test
  void shouldUpdateItemQuantity() {
    // First add an item
    panierVariantRepo.save(PanierVariant.builder().panier(Panier.builder().id(testPanierId).build())
        .variant(Variant.builder().id(testVariantId).build()).quantite(1L).build());

    CartItemRequest item = new CartItemRequest();
    item.setId(testProductId);
    item.setQuantite(3L);

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(item).when()
        .put("/v1/utilisateur/{userId}/panier/items/{productId}", userId, testVariantId).then()
        .log().all(true).statusCode(HttpStatus.OK.value())
        .body("articles[0].variants[0].reservedQuantity", equalTo(3));
  }

  @Test
  void shouldRemoveItemFromCart() {
    // First add an item
    panierVariantRepo.save(PanierVariant.builder().panier(Panier.builder().id(testPanierId).build())
        .variant(Variant.builder().id(testVariantId).build()).quantite(1L).build());

    given().cookie("jwt", jwtToken).when()
        .delete("/v1/utilisateur/{userId}/panier/items/{productId}", userId, testVariantId).then()
        .log().all(true).statusCode(HttpStatus.OK.value());
  }

  @Test
  void shouldRemoveAllItemsFromCart() {
    // First add items
    panierVariantRepo.save(PanierVariant.builder().panier(Panier.builder().id(testPanierId).build())
        .variant(Variant.builder().id(testVariantId).build()).quantite(1L).build());

    given().cookie("jwt", jwtToken).when()
        .delete("/v1/utilisateur/{userId}/panier/{panierId}", userId, testPanierId).then().log()
        .all(true).statusCode(HttpStatus.OK.value());
  }

  @Test
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  void shouldDeletePanier() {
    given().cookie("jwt", jwtToken).when().delete("/v1/paniers/" + testPanierId).then()
        .statusCode(HttpStatus.OK.value());
  }

  @Test
  void shouldMergeCart() {
    CartItemRequest item = new CartItemRequest();
    item.setId(testVariantId);
    item.setQuantite(2L);

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(List.of(item)).when()
        .post("/v1/utilisateur/{userId}/panier/merge", userId).then().log().all(true)
        .statusCode(HttpStatus.OK.value())
        .body("articles[0].id", equalTo(testProductId.intValue()));
  }

  @Test
  void shouldReturnNotFoundWhenMergingWithInvalidProduct() {
    CartItemRequest item = new CartItemRequest();
    item.setId(999L); // Invalid product ID
    item.setQuantite(1L);

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(List.of(item)).when()
        .post("/v1/utilisateur/{userId}/panier/merge", userId).then()
        .statusCode(HttpStatus.NOT_FOUND.value());
  }
}
