package tn.temporise.infrastructure.repository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.ImageProductMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.ImageProduit;
import tn.temporise.domain.port.ImageProduitRepo;
import tn.temporise.infrastructure.persistence.entity.ImageProduitEntity;

import java.util.List;

@Transactional
@Repository
@RequiredArgsConstructor
public class ImageProduitImp implements ImageProduitRepo {
  private final ImageProduitJpaRepo imageProduitJpaRepo;
  private final ImageProductMapper imageProductMapper;
  private final ExceptionFactory exceptionFactory;

  @Override
  public ImageProduit save(ImageProduit imageProduit) {
    ImageProduitEntity imageProduitEntity = imageProductMapper.modelToEntity(imageProduit);

    return imageProductMapper.entityToModel(imageProduitJpaRepo.save(imageProduitEntity));
  }

  @Override
  public ImageProduit findById(Long id) {
    ImageProduitEntity imageProduitEntity = imageProduitJpaRepo.findById(id).orElse(null);
    return imageProductMapper.entityToModel(imageProduitEntity);
  }

  @Override
  public List<ImageProduit> findAll() {
    return imageProduitJpaRepo.findAll().stream().map(imageProductMapper::entityToModel).toList();
  }

  @Override
  public ImageProduit update(ImageProduit imageProduit) {
    ImageProduitEntity imageProduitEntity = imageProductMapper.modelToEntity(imageProduit);
    return imageProductMapper.entityToModel(imageProduitEntity);
  }

  @Override
  public void deleteById(Long id) {
    imageProduitJpaRepo.deleteById(id);
  }

  @Override
  public void deleteAll() {
    imageProduitJpaRepo.deleteAll();
  }

  @Override
  public List<ImageProduit> findByProduitId(Long id) {
    List<ImageProduitEntity> imageProduitEntities =
        imageProduitJpaRepo.findByProduitId(id).orElse(null);
    if (imageProduitEntities == null)
      throw exceptionFactory.notFound("notfound.no_images");
    return imageProduitEntities.stream().map(imageProductMapper::entityToModel).toList();
  }
}
