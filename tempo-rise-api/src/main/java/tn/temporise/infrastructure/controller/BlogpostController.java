package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.BlogPostService;
import tn.temporise.domain.model.ArticleRequest;
import tn.temporise.domain.model.ArticleResponse;
import tn.temporise.domain.model.Response;
import tn.temporise.infrastructure.api.ArticlesApi;

import java.util.List;


@RestController
@Slf4j
@RequiredArgsConstructor

public class BlogpostController implements ArticlesApi {
  private final BlogPostService blogPostService;

  @Override
  public ResponseEntity<ArticleResponse> _createArticle(ArticleRequest articleRequest)
      throws Exception {
    ArticleResponse blogPost = blogPostService.createBlogPost(articleRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(blogPost);
  }

  @Override
  public ResponseEntity<Response> _deleteArticle(Long id) throws Exception {
    blogPostService.deleteBlogPost(id);
    Response response = new Response();
    response.setCode("200");
    response.setMessage("Toutes les catégories ont été supprimés avec succès");
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<List<ArticleResponse>> _getAllArticles() throws Exception {
    List<ArticleResponse> responses = blogPostService.getAllBlogPost();
    return ResponseEntity.ok(responses);
  }

  @Override
  public ResponseEntity<ArticleResponse> _getArticleById(Long id) throws Exception {
    log.info("Received ID: " + id);
    ArticleResponse blogPost = blogPostService.getBlogPostById(id);
    return ResponseEntity.ok(blogPost);
  }

  public ResponseEntity<ArticleResponse> _updateArticle(Long id, ArticleRequest articleRequest)
      throws Exception {
    ArticleResponse blogPost = blogPostService.updateBlogPost(id, articleRequest);
    return ResponseEntity.ok(blogPost);
  }


}
