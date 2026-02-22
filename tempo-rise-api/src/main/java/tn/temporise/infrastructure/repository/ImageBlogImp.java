package tn.temporise.infrastructure.repository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.BlogPostMapper;
import tn.temporise.application.mapper.ImageBlogMapper;

import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.ImageBlogPost;

import tn.temporise.domain.port.BlogPostRepo;
import tn.temporise.domain.port.ImageBlogRepo;
import tn.temporise.infrastructure.persistence.entity.ImageBlogPostEntity;

import java.util.List;

@Transactional
@Repository
@RequiredArgsConstructor

public class ImageBlogImp implements ImageBlogRepo {
  private final ImageBlogJpaRepo imageBlogJpaRepo;
  private final BlogPostRepo blogPostRepo;
  private final BlogPostMapper blogPostMapper;
  private final ImageBlogMapper imageBlogMapper;
  private final ExceptionFactory exceptionFactory;

  @Override
  public ImageBlogPost save(ImageBlogPost imageBlogPost) {
    ImageBlogPostEntity imageBlogPostEntity = imageBlogMapper.modelToEntity(imageBlogPost);
    // BlogPost blogPost=blogPostRepo.findById(imageBlogPost.blogPost().id());
    // imageBlogPostEntity.setBlogPost(blogPostMapper.modelToEntity(blogPost));
    return imageBlogMapper.entityToModel(imageBlogJpaRepo.save(imageBlogPostEntity));
  }

  @Override
  public ImageBlogPost findById(Long id) {
    ImageBlogPostEntity imageBlogPostEntity = imageBlogJpaRepo.findById(id)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.image"));
    return imageBlogMapper.entityToModel(imageBlogPostEntity);
  }

  @Override
  public List<ImageBlogPost> findAll() {
    return imageBlogJpaRepo.findAll().stream().map(imageBlogMapper::entityToModel).toList();
  }

  @Override
  public ImageBlogPost update(ImageBlogPost imageBlogPost) {
    ImageBlogPostEntity imageBlogPostEntity = imageBlogMapper.modelToEntity(imageBlogPost);
    return imageBlogMapper.entityToModel(imageBlogPostEntity);
  }


  @Override
  public void deleteById(Long id) {
    imageBlogJpaRepo.deleteById(id);
  }

  @Override
  public void deleteAll() {
    imageBlogJpaRepo.deleteAll();
  }

  @Override
  public List<ImageBlogPost> findByProduitId(Long id) {
    List<ImageBlogPostEntity> imageProduitEntities =
        imageBlogJpaRepo.findByProduitId(id).orElse(null);
    if (imageProduitEntities == null)
      throw exceptionFactory.notFound("notfound.no_images");
    return imageProduitEntities.stream().map(imageBlogMapper::entityToModel).toList();
  }
}
