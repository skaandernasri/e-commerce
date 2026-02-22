package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.BlogPostMapper;

import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.BlogPost;

import tn.temporise.domain.port.BlogPostRepo;
import tn.temporise.infrastructure.persistence.entity.BlogPostEntity;


import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BlogPostRepoImp implements BlogPostRepo {
  private final BlogPostJpaRepo blogPostJpaRepo;
  private final BlogPostMapper blogPostMapper;
  private final ExceptionFactory exceptionFactory;

  @Override
  public BlogPost save(BlogPost blogPost) {
    BlogPostEntity blogPostEntity = blogPostJpaRepo.save(blogPostMapper.modelToEntity(blogPost));
    return blogPostMapper.entityToModel(blogPostEntity);
  }

  @Override
  public BlogPost findById(Long id) {
    return blogPostJpaRepo.findById(id).map(blogPostMapper::entityToModel)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.blogpost"));
  }

  @Override
  public List<BlogPost> findAll() {
    log.info("blogposentitytlist {}", blogPostJpaRepo.findAll());
    return blogPostJpaRepo.findAll().stream().map(blogPostMapper::entityToModel)
        .collect(Collectors.toList());
  }

  @Override
  public BlogPost update(BlogPost blogPost) {
    return blogPostJpaRepo.findById(blogPost.id()).map(existingEntity -> {
      BlogPostEntity updatedEntity = blogPostMapper.modelToEntity(blogPost);
      updatedEntity.setId(existingEntity.getId()); // Assurer la conservation de l'ID
      return blogPostMapper.entityToModel(blogPostJpaRepo.save(updatedEntity));
    }).orElseThrow(() -> exceptionFactory.notFound("notfound.category"));
  }

  @Override
  public void deleteById(Long id) {
    blogPostJpaRepo.deleteById(id);
  }

  @Override
  public void deleteAll() {
    blogPostJpaRepo.deleteAll();
  }



}
