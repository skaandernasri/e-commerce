package tn.temporise.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.BlogPostMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.BlogPost;
import tn.temporise.domain.model.BlogPostStatus;
import tn.temporise.domain.port.BlogPostRepo;
import tn.temporise.domain.model.ArticleRequest;
import tn.temporise.domain.model.ArticleResponse;


import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogPostService {
  private final BlogPostRepo blogPostRepo;
  private final BlogPostMapper blogPostMapper;
  private final ExceptionFactory exceptionFactory;
  private final ImageBlogService imageBlogService;
  private final UploadImageService uploadImageService;


  public ArticleResponse createBlogPost(ArticleRequest articleRequest) {
    try {
      if (articleRequest == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }


      BlogPost blogPost = blogPostMapper.dtoToModel(articleRequest);
      if (blogPost.status().equals(BlogPostStatus.PUBLIER))
        blogPost = blogPost.toBuilder().date_publication(LocalDateTime.now()).build();

      BlogPost savedBlogPost = blogPostRepo.save(blogPost);

      if (savedBlogPost == null) {
        throw exceptionFactory.internalServerError("internal.server_error",
            "Category creation failed");
      }

      return blogPostMapper.modelToResponse(savedBlogPost);

    } catch (BadRequestException e) {
      log.warn("Category creation failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Category creation failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Category creation failed" + e.getMessage());
    }
  }

  public ArticleResponse getBlogPostById(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      BlogPost blogPost = blogPostRepo.findById(id);
      if (blogPost == null) {
        throw exceptionFactory.notFound("notfound.category");
      }
      return blogPostMapper.modelToResponse(blogPost);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Category retrieval failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Category retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Category retrieval failed" + e.getMessage());
    }
  }

  public List<ArticleResponse> getAllBlogPost() {
    try {
      List<ArticleResponse> BlogPostList =
          blogPostRepo.findAll().stream().map(blogPostMapper::modelToResponse).toList();
      log.info("blogpostlist {}", blogPostRepo.findAll());

      if (BlogPostList.isEmpty()) {
        throw exceptionFactory.notFound("notfound.no_categories");
      }
      return BlogPostList;

    } catch (NotFoundException e) {
      log.warn("No categories found", e);
      throw e;
    } catch (Exception e) {
      log.error("Category list retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Category list retrieval failed" + e.getMessage());
    }
  }

  @Transactional
  public ArticleResponse updateBlogPost(Long id, ArticleRequest articleRequest) {
    try {
      if (id == null || articleRequest == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }

      BlogPost existingBlogPost = blogPostRepo.findById(id);
      if (existingBlogPost.user() == null) {
        throw exceptionFactory.notFound("notfound.user");
      }
      BlogPost blogPost = blogPostMapper.dtoToModel(articleRequest);
      if (blogPost.status().equals(BlogPostStatus.PUBLIER))
        blogPost = blogPost.toBuilder().date_publication(LocalDateTime.now()).build();
      BlogPost updatedBlogPost = existingBlogPost.toBuilder().titre(blogPost.titre())
          .contenu(blogPost.contenu()).status(blogPost.status()).image(blogPost.image())
          .date_publication(blogPost.date_publication()).build();
      updatedBlogPost = blogPostRepo.save(updatedBlogPost);
      uploadImageService.deleteByOldImage(updatedBlogPost.contenu(), existingBlogPost.contenu());
      return blogPostMapper.modelToResponse(updatedBlogPost);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Category update failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Category update failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Category update failed" + e.getMessage());
    }
  }

  @Transactional
  public void deleteBlogPost(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      BlogPost blogPost = blogPostRepo.findById(id);
      log.info("blogPost {}", blogPost);
      if (blogPost.image() != null) {
        blogPost.image().forEach(image -> {
          imageBlogService.deleteImage(image.id());
        });
      }
      uploadImageService.deleteByContent(blogPost.contenu());


      blogPostRepo.deleteById(id);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Category deletion failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Category deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Category deletion failed" + e.getMessage());
    }
  }

  public void deleteAllBlogPost() {
    try {
      List<BlogPost> categorieList = blogPostRepo.findAll();
      if (categorieList.isEmpty()) {
        throw exceptionFactory.notFound("notfound.no_categories");
      }
      blogPostRepo.deleteAll();

    } catch (NotFoundException e) {
      log.warn("No categories to delete", e);
      throw e;
    } catch (Exception e) {
      log.error("Category bulk deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Category bulk deletion failed" + e.getMessage());
    }
  }



}
