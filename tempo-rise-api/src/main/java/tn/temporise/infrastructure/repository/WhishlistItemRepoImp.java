package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.WhishlistItemMapper;
import tn.temporise.domain.model.WhishlistItem;
import tn.temporise.domain.port.WhishlistItemRepo;
import tn.temporise.infrastructure.persistence.entity.WhishlistItemEntity;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class WhishlistItemRepoImp implements WhishlistItemRepo {
  private final WhishlistItemJpaRepo whishlistItemJpaRepo;
  private final WhishlistItemMapper whishlistItemMapper;

  @Override
  public WhishlistItem save(WhishlistItem whishlistItem) {
    WhishlistItemEntity whishlistEntity =
        whishlistItemJpaRepo.save(whishlistItemMapper.modelToEntity(whishlistItem));
    return whishlistItemMapper.entityToModel(whishlistEntity);
  }

  @Override
  public List<WhishlistItem> findByWhishlistId(Long whishlistId) {
    return whishlistItemJpaRepo.findAllByWhishlistId(whishlistId).stream()
        .map(whishlistItemMapper::entityToModel).toList();
  }

  @Override
  public boolean existsByWhishlistIdAndProduitId(Long whishlistId, Long produitId) {
    return whishlistItemJpaRepo.existsByWhishlistIdAndProduitId(whishlistId, produitId);

  }

  @Override
  public void removeAllItems(Long userId) {
    whishlistItemJpaRepo.deleteAllByWhishlistUtilisateurId(userId);
  }

  @Override
  public void removeItem(Long userId, Long productId) {
    whishlistItemJpaRepo.deleteByWhishlistUtilisateurIdAndProduitId(userId, productId);
  }
}
