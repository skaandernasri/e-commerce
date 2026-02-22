package tn.temporise.ti;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tn.temporise.domain.model.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class CommandeControllerIntegrationTest extends BaseIntegrationTest {

  private Long testUserId;
  private Long testCommandeId;
  private Long adresseLivraisonId;
  private Long adresseFacturationId;
  private Long variantId;
  private Long productId;
  private Long categorieId;


  @BeforeEach
  void setUpTestData() {
    // Create test user
    UtilisateurModel testUser = UtilisateurModel.builder().id(null).email("user@test.com")
        .roles(Set.of(Role.CLIENT)).isverified(true).build();
    UtilisateurModel savedUser = userRepo.save(testUser);
    testUserId = savedUser.id();

    // Create category
    Categorie category = new Categorie(null, "Test Category", "Test Desc");
    Categorie savedCategory = categorieRepo.save(category);
    categorieId = savedCategory.id();
    // Create product and variant
    Produit product = Produit.builder().nom("Test Product").description("Desc").prix(100.0)
        .categorie(savedCategory).build();
    Produit savedProduct = productRepo.save(product);
    productId = savedProduct.id();
    Variant variant = Variant.builder().color("red").size("M").produit(savedProduct).quantity(10L)
        .availableQuantity(0L).build();
    Variant savedVariant = variantRepo.save(variant);
    variantId = savedVariant.id();
    // Create panier and add variant
    Panier panier = Panier.builder().utilisateur(savedUser).build();
    Panier savedPanier = panierRepo.save(panier);
    PanierVariant panierVariant = PanierVariant.builder().panier(savedPanier).variant(savedVariant)
        .quantite(2L).expirationDate(LocalDateTime.now().plusMinutes(20)).build();
    panierVariantRepo.save(panierVariant);
    Panier panier1 =
        Panier.builder().utilisateur(UtilisateurModel.builder().id(userId).build()).build();
    Panier savedPanier1 = panierRepo.save(panier1);
    PanierVariant panierVariant1 =
        PanierVariant.builder().panier(savedPanier1).variant(savedVariant).quantite(2L)
            .expirationDate(LocalDateTime.now().plusMinutes(20)).build();
    panierVariantRepo.save(panierVariant1);

    // Create promo
    CodePromo codePromo = CodePromo.builder().code("SUMMER20").reduction(20.0)
        .dateExpiration(LocalDateTime.now(ZoneOffset.UTC).plusSeconds(86400)).build();
    codePromoRepo.save(codePromo);

    // Create addresses
    Adresse livraison = Adresse.builder().type(TypeAdresse.LIVRAISON).pays("Tunisia").ville("Tunis")
        .codePostal("1000").ligne1("Rue de la paix").build();
    Adresse facturation = livraison.toBuilder().type(TypeAdresse.FACTURATION).build();
    Adresse savedLivraison = adresseRepo.save(livraison);
    Adresse savedFacturation = adresseRepo.save(facturation);
    adresseLivraisonId = adresseRepo.save(livraison).id();
    adresseFacturationId = adresseRepo.save(facturation).id();

    // Create commande + ligneCommande for get tests
    Commande commande = Commande.builder().user(savedUser).statut(StatutCommande.EN_COURS)
        .nom("Test").total(200.0).codePromo(codePromo).prenom("John").telephone("22222222")
        .email("test@facture.com").date(LocalDateTime.now(ZoneOffset.UTC)).modePaiement(null)
        .adresseLivraison(savedLivraison).adresseFacturation(savedFacturation).build();
    Commande savedCommande = commandeRepo.save(commande);
    testCommandeId = savedCommande.id();

    LigneCommande ligne = LigneCommande.builder().commande(savedCommande).variant(savedVariant)
        .quantite(2).prixTotal(200.0).build();
    ligneCommandeRepo.save(ligne);
  }

  @Test
  void shouldCreateCommande() {
    PanierResponseArticlesInnerCategorie categorie = new PanierResponseArticlesInnerCategorie();
    categorie.setId(categorieId);
    categorie.setNom("Test Category");
    categorie.setDescription("aedzadaz");

    CommandeRequestProduitsInnerVariantsInner variantResponse =
        new CommandeRequestProduitsInnerVariantsInner();
    variantResponse.setId(variantId);
    variantResponse.setReservedQuantity(2L);

    CommandeRequestProduitsInner produit = new CommandeRequestProduitsInner();
    produit.setVariants(List.of(variantResponse));
    produit.setNewPrice(200D);
    produit.setPrixTotal(400D);
    produit.setPrix(200D);
    produit.setVariants(List.of(variantResponse));
    produit.setId(productId);

    CommandeRequest request = new CommandeRequest();
    request.produits(List.of(produit));
    request.utilisateurId(userId);
    request.nom("Test Commande");
    request.prenom("John");
    request.email("test@facture.com");
    request.telephone("123456789"); // ✅ keep as phone
    request.token("TND"); // ✅ correct field
    request.codePromo("SUMMER20");
    request.shipping(20.0);
    request.total(500D);
    request.modePaiement(ModePaiementEnum.A_LIVRAISON);
    request.adresseFacturationId(adresseFacturationId);
    request.adresseLivraisonId(adresseLivraisonId);

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .post("/v1/commandes").then().log().all(true).statusCode(HttpStatus.CREATED.value()) // ✅
                                                                                             // will
                                                                                             // now
                                                                                             // pass
        .body("paymentRef", notNullValue());
  }


  @Test
  void shouldGetCommandeById() {
    given().queryParam("user_id", testUserId).when().get("/v1/commandes/" + testCommandeId).then()
        .statusCode(HttpStatus.OK.value()).body("id", equalTo(testCommandeId.intValue()))
        .body("utilisateur.id", equalTo(testUserId.intValue()));
  }

  @Test
  void shouldGetAllCommandes() {
    given().cookie("jwt", jwtToken).when().get("/v1/commandes").then().log().all(true)
        .statusCode(HttpStatus.OK.value()).body("$", hasSize(greaterThanOrEqualTo(1)));
  }

  @Test
  void shouldGetCommandesByUserId() {
    given().cookie("jwt", jwtToken).queryParam("id", testUserId).when()
        .get("/v1/commandes/utilisateur").then().log().all(true).statusCode(HttpStatus.OK.value())
        .body("$", hasSize(greaterThanOrEqualTo(1)))
        .body("[0].utilisateur.id", equalTo(testUserId.intValue()));
  }

  @Test
  void shouldFailCreateCommandeWithInvalidPayment() {
    CommandeRequest request = new CommandeRequest();
    request.setUtilisateurId(testUserId); // No token or invalid token

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .post("/v1/commandes").then().statusCode(HttpStatus.BAD_REQUEST.value());
  }
}
