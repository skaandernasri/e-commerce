package tn.temporise.ti;

import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import tn.temporise.domain.model.*;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Slf4j
class FactureControllerIntegrationTest extends BaseIntegrationTest {

  private Long testCommandeId;
  private Long testCommandeId1;

  private Long testFactureId;
  private Long testUserId;

  @BeforeEach
  void setUpTestData() {
    // Create test user
    UtilisateurModel testUser =
        UtilisateurModel.builder().email("test@facture.com").password("password").build();
    UtilisateurModel savedUser = userRepo.save(testUser);
    testUserId = savedUser.id();

    // Create addresses
    Adresse savedAdresseLivraison = adresseRepo.save(Adresse.builder().utilisateur(savedUser)
        .ville("Tunis").pays("Tunisie").type(TypeAdresse.LIVRAISON).codePostal("1000")
        .ligne1("Ligne 1").ligne2("Ligne 2").build());

    Adresse savedAdresseFacturation = adresseRepo.save(Adresse.builder().utilisateur(savedUser)
        .ville("Tunis").pays("Tunisie").type(TypeAdresse.FACTURATION).codePostal("1000")
        .ligne1("Ligne 1").ligne2("Ligne 2").build());

    // Create test commande
    Commande testCommande = Commande.builder().email("test@facture.com").prenom("Test")
        .telephone("123456789").nom("Test Commande").user(savedUser).statut(StatutCommande.LIVREE)
        .modePaiement(null).adresseFacturation(savedAdresseFacturation)
        .adresseLivraison(savedAdresseLivraison).total(100.0).build();
    Commande savedCommande = commandeRepo.save(testCommande);
    testCommandeId = savedCommande.id();
    Commande testCommande1 = Commande.builder().email("test@facture.com").prenom("Test")
        .telephone("123456789").nom("Test Commande 1").user(savedUser).statut(StatutCommande.LIVREE)
        .modePaiement(null).adresseFacturation(savedAdresseFacturation)
        .adresseLivraison(savedAdresseLivraison).total(100.0).build();
    Commande savedCommande1 = commandeRepo.save(testCommande1);
    testCommandeId1 = savedCommande1.id();
    // Create test facture
    Facture testFacture = Facture.builder().commande(savedCommande).total(100.0).build();
    Facture savedFacture = factureRepo.save(testFacture);
    testFactureId = savedFacture.id();

    // Update commande with facture reference if bidirectional
    Commande updatedCommande = savedCommande.toBuilder().facture(savedFacture).build();
    commandeRepo.save(updatedCommande);
  }

  @Test
  void shouldCreateFacture() {

    FactureRequest request = new FactureRequest();
    request.setCommandeId(testCommandeId1);
    request.setTotal(150.0);
    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).body(request).when()
        .post("/v1/factures").then().log().all(true).statusCode(HttpStatus.OK.value())
        .body("commande.id", equalTo(testCommandeId1.intValue())).body("total", equalTo(150.0f));
  }

  @Test
  void shouldGetFactureById() {
    given().cookie("jwt", jwtToken).when().get("/v1/factures/" + testFactureId).then()
        .statusCode(HttpStatus.OK.value()).body("id", equalTo(testFactureId.intValue()))
        .body("total", equalTo(100.0f));
  }

  @Test
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  void shouldGetAllFactures() {
    given().cookie("jwt", jwtToken).when().get("/v1/factures").then()
        .statusCode(HttpStatus.OK.value()).body("size()", greaterThan(0))
        .body("[0].id", equalTo(testFactureId.intValue()));
  }

  // @Test
  // void shouldGetFacturesByCommandeId() {
  // given().cookie("jwt", jwtToken).when().get("/v1/factures/commande/" + testCommandeId).then()
  // .statusCode(HttpStatus.OK.value()).body("size()", greaterThan(0))
  // .body("[0].commande.id", equalTo(testCommandeId.intValue()));
  // }

  @Test
  void shouldGetFacturesByUserId() {
    given().cookie("jwt", jwtToken).when().get("/v1/factures/utilisateur/" + testUserId).then()
        .log().all(true).statusCode(HttpStatus.OK.value()).body("size()", greaterThan(0))
        .body("[0].commande.utilisateur.email", equalTo("test@facture.com"));
  }

  @Test
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  void shouldDeleteFacture() {
    given().cookie("jwt", jwtToken).when().delete("/v1/factures/" + testFactureId).then()
        .statusCode(HttpStatus.OK.value()).body("code", equalTo("200"))
        .body("message", equalTo("Facture supprimée avec succès"));
  }

  @Test
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  void shouldDeleteAllFactures() {
    given().cookie("jwt", jwtToken).when().delete("/v1/factures").then()
        .statusCode(HttpStatus.OK.value()).body("code", equalTo("200"))
        .body("message", equalTo("Toutes les factures ont été supprimées avec succès"));
  }

  @Test
  void shouldReturnNotFoundForInvalidFactureId() {
    given().cookie("jwt", jwtToken).when().get("/v1/factures/99999").then()
        .statusCode(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void shouldReturnBadRequestForInvalidCommandeId() {

    given().cookie("jwt", jwtToken).contentType(ContentType.JSON).when()
        .get("/v1/factures/commande/99999").then().log().all(true)
        .statusCode(HttpStatus.NOT_FOUND.value());
  }
}
