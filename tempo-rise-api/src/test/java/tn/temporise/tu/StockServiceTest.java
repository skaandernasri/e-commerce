package tn.temporise.tu;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tn.temporise.application.exception.client.ConflictException;
import tn.temporise.application.service.StockService;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.PanierVariantRepo;
import tn.temporise.domain.port.VariantRepo;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StockServiceTest {
  @InjectMocks
  private StockService stockService;

  @Mock
  private VariantRepo variantRepo;

  @Mock
  private PanierVariantRepo panierVariantRepo;

  @Mock
  private ExceptionFactory exceptionFactory;

  @Test
  void updateStockForCommandeReturnSuccess() {
    Variant variant = Variant.builder().id(1L).quantity(10L).build();
    LigneCommande ligneCommande =
        LigneCommande.builder().variant(Variant.builder().id(1L).build()).quantite(1L).build();
    Commande commande =
        Commande.builder().id(1L).total(500D).lignesCommande(List.of(ligneCommande)).build();
    when(variantRepo.findByIdForUpdate(1L)).thenReturn(variant);
    stockService.updateStockForCommande(commande, true);
  }

  @Test
  void updateStockForCommandeNoReturnSuccess() {
    UtilisateurModel utilisateurModel = UtilisateurModel.builder().id(1L).build();
    Variant variant = Variant.builder().id(1L).quantity(10L).build();
    LigneCommande ligneCommande =
        LigneCommande.builder().variant(Variant.builder().id(1L).build()).quantite(1L).build();
    Commande commande = Commande.builder().id(1L).total(500D).lignesCommande(List.of(ligneCommande))
        .user(utilisateurModel).build();
    when(panierVariantRepo.findByVariantIdAndUserIdDifferent(1L, 1L))
        .thenReturn(Collections.emptyList());
    when(variantRepo.findByIdForUpdate(1L)).thenReturn(variant);
    stockService.updateStockForCommande(commande, false);
  }

  @Test
  void updateStockForCommandeNoReturnFailed() {
    UtilisateurModel utilisateurModel = UtilisateurModel.builder().id(1L).build();
    Variant variant = Variant.builder().id(1L).quantity(10L).build();
    LigneCommande ligneCommande =
        LigneCommande.builder().variant(Variant.builder().id(1L).build()).quantite(1L).build();
    Commande commande = Commande.builder().id(1L).total(500D).lignesCommande(List.of(ligneCommande))
        .user(utilisateurModel).build();
    PanierVariant panierVariant = PanierVariant.builder().variant(variant)
        .panier(Panier.builder().id(1L).variants(Set.of(variant)).build()).quantite(10L).build();
    when(panierVariantRepo.findByVariantIdAndUserIdDifferent(1L, 1L))
        .thenReturn(List.of(panierVariant));
    when(variantRepo.findByIdForUpdate(1L)).thenReturn(variant);
    when(exceptionFactory.conflict("conflict.insufficient_stock"))
        .thenReturn(new ConflictException("conflict.insufficient_stock", ""));
    assertThrows(ConflictException.class,
        () -> stockService.updateStockForCommande(commande, false));

  }


}
