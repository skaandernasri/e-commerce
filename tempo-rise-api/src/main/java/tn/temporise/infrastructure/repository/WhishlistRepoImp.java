package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.WhishlistMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.Whishlist;
import tn.temporise.domain.port.WhishlistRepo;
import tn.temporise.infrastructure.persistence.entity.WhishlistEntity;

@Repository
@RequiredArgsConstructor
@Slf4j
public class WhishlistRepoImp implements WhishlistRepo {
  private final WhishlistJpaRepo whishlistJpaRepo;
  private final WhishlistMapper whishlistMapper;
  private final ExceptionFactory exceptionFactory;

  @Override
  public Whishlist save(Whishlist whishlist) {
    WhishlistEntity whishlistEntity =
        whishlistJpaRepo.save(whishlistMapper.modelToEntity(whishlist));
    return whishlistMapper.entityToModel(whishlistEntity);
  }

  @Override
  public Whishlist findById(Long id) {
    return whishlistMapper.entityToModel(whishlistJpaRepo.findById(id)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.whishlist")));
  }

  @Override
  public Whishlist findByUserId(Long userId) {
    return whishlistMapper.entityToModel(whishlistJpaRepo.findByUtilisateurId(userId)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.whishlist")));
  }
}
