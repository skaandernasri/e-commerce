package tn.temporise.tu;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.exception.server.InternalServerErrorException;
import tn.temporise.application.mapper.BlogPostMapper;
import tn.temporise.application.service.BlogPostService;
import tn.temporise.application.service.UploadImageService;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.BlogPostRepo;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class BlogPostServiceTest {

  @Mock
  private BlogPostRepo blogPostRepo;

  @Mock
  private BlogPostMapper blogPostMapper;

  @Mock
  private ExceptionFactory exceptionFactory;
  @Mock
  private UploadImageService uploadImageService;

  @InjectMocks
  private BlogPostService blogPostService;

  private ArticleRequest articleRequest;
  private ArticleResponse articleResponse;
  private BlogPost blogPost;

  @BeforeEach
  void setUp() {
    articleRequest = new ArticleRequest();
    articleRequest.setTitre("Test Title");
    articleRequest.setContenu("Test Content");
    articleRequest.setStatus(ArticleRequest.StatusEnum.PUBLIER.PUBLIER);
    articleRequest.auteur("1");

    blogPost = BlogPost.builder().id(1L).titre("Test Title").contenu("Test Content")
        .status(BlogPostStatus.PUBLIER).date_publication(LocalDateTime.now())
        .user(UtilisateurModel.builder().id(1L).build()).build();

    articleResponse = new ArticleResponse();
    articleResponse.setId(1L);
    articleResponse.setTitre("Test Title");
    articleResponse.setContenu("Test Content");
  }

  // ---------- CREATE ----------
  @Test
    void testCreateBlogPost_Success() {
        when(blogPostMapper.dtoToModel(articleRequest)).thenReturn(blogPost);
        when(blogPostRepo.save(any(BlogPost.class))).thenReturn(blogPost);
        when(blogPostMapper.modelToResponse(blogPost)).thenReturn(articleResponse);

        ArticleResponse result = blogPostService.createBlogPost(articleRequest);

        assertNotNull(result);
        assertEquals(articleResponse.getTitre(), result.getTitre());
        verify(blogPostRepo, times(1)).save(any(BlogPost.class));
    }

  @Test
    void testCreateBlogPost_NullRequest() {
        when(exceptionFactory.badRequest("badrequest.invalid_input"))
                .thenReturn(new BadRequestException("badrequest.invalid_input", "400"));

        assertThrows(BadRequestException.class, () -> blogPostService.createBlogPost(null));
        verify(exceptionFactory, times(1)).badRequest("badrequest.invalid_input");
    }

  @Test
    void testCreateBlogPost_SaveReturnsNull() {
        when(blogPostMapper.dtoToModel(articleRequest)).thenReturn(blogPost);
        when(blogPostRepo.save(blogPost)).thenReturn(null);
        when(exceptionFactory.internalServerError(any(), any()))
                .thenReturn(new InternalServerErrorException("internal error","5000"));

        assertThrows(RuntimeException.class, () -> blogPostService.createBlogPost(articleRequest));
    }

  // ---------- GET BY ID ----------
  @Test
    void testGetBlogPostById_Success() {
        when(blogPostRepo.findById(1L)).thenReturn(blogPost);
        when(blogPostMapper.modelToResponse(blogPost)).thenReturn(articleResponse);

        ArticleResponse result = blogPostService.getBlogPostById(1L);

        assertNotNull(result);
        assertEquals(articleResponse.getTitre(), result.getTitre());
    }

  @Test
    void testGetBlogPostById_NullId() {
        when(exceptionFactory.badRequest("badrequest.invalid_id"))
                .thenReturn(new BadRequestException("Invalid ID", "4000"));

        assertThrows(BadRequestException.class, () -> blogPostService.getBlogPostById(null));
        verify(exceptionFactory, times(1)).badRequest("badrequest.invalid_id");
    }

  @Test
    void testGetBlogPostById_NotFound() {
        when(blogPostRepo.findById(1L)).thenReturn(null);
        when(exceptionFactory.notFound("notfound.category"))
                .thenReturn(new NotFoundException("Not found", "404"));

        assertThrows(NotFoundException.class, () -> blogPostService.getBlogPostById(1L));
    }

  // ---------- GET ALL ----------
  @Test
    void testGetAllBlogPost_Success() {
        when(blogPostRepo.findAll()).thenReturn(List.of(blogPost));
        when(blogPostMapper.modelToResponse(blogPost)).thenReturn(articleResponse);

        List<ArticleResponse> result = blogPostService.getAllBlogPost();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

  @Test
    void testGetAllBlogPost_EmptyList() {
        when(blogPostRepo.findAll()).thenReturn(List.of());
        when(exceptionFactory.notFound("notfound.no_categories"))
                .thenReturn(new NotFoundException("Empty", "404"));

        assertThrows(NotFoundException.class, () -> blogPostService.getAllBlogPost());
    }

  // ---------- UPDATE ----------
  @Test
    void testUpdateBlogPost_Success() {
        when(blogPostRepo.findById(1L)).thenReturn(blogPost);
        when(blogPostMapper.dtoToModel(articleRequest)).thenReturn(blogPost);
        when(blogPostRepo.save(any())).thenReturn(blogPost);
        when(blogPostMapper.modelToResponse(blogPost)).thenReturn(articleResponse);

        ArticleResponse result = blogPostService.updateBlogPost(1L, articleRequest);

        assertNotNull(result);
        assertEquals("Test Title", result.getTitre());
    }

  @Test
    void testUpdateBlogPost_NullId() {
        when(exceptionFactory.badRequest("badrequest.invalid_input"))
                .thenReturn(new BadRequestException("Invalid input", "4000"));

        assertThrows(BadRequestException.class, () -> blogPostService.updateBlogPost(null, articleRequest));
    }

  @Test
  void testUpdateBlogPost_UserNotFound() {
    BlogPost existing = blogPost.toBuilder().user(null).build();
    when(blogPostRepo.findById(1L)).thenReturn(existing);
    when(exceptionFactory.notFound("notfound.user"))
        .thenReturn(new NotFoundException("User not found", "404"));

    assertThrows(NotFoundException.class, () -> blogPostService.updateBlogPost(1L, articleRequest));
  }

  // ---------- DELETE ----------
  @Test
    void testDeleteBlogPost_Success() {
        when(blogPostRepo.findById(1L)).thenReturn(blogPost);
        assertDoesNotThrow(() -> blogPostService.deleteBlogPost(1L));
        verify(blogPostRepo, times(1)).deleteById(1L);
    }

  @Test
    void testDeleteBlogPost_NullId() {
        when(exceptionFactory.badRequest("badrequest.invalid_id"))
                .thenReturn(new BadRequestException("Invalid ID", "4000"));

        assertThrows(BadRequestException.class, () -> blogPostService.deleteBlogPost(null));
    }

  @Test
    void testDeleteBlogPost_NotFound() {
        when(blogPostRepo.findById(1L)).thenThrow(new NotFoundException("Blog not found", "404"));

        assertThrows(NotFoundException.class, () -> blogPostService.deleteBlogPost(1L));
    }

  // ---------- DELETE ALL ----------
  @Test
    void testDeleteAllBlogPost_Success() {
        when(blogPostRepo.findAll()).thenReturn(List.of(blogPost));
        assertDoesNotThrow(() -> blogPostService.deleteAllBlogPost());
        verify(blogPostRepo, times(1)).deleteAll();
    }

  @Test
    void testDeleteAllBlogPost_Empty() {
        when(blogPostRepo.findAll()).thenReturn(List.of());
        when(exceptionFactory.notFound("notfound.no_categories"))
                .thenReturn(new NotFoundException("Empty", "404"));

        assertThrows(NotFoundException.class, () -> blogPostService.deleteAllBlogPost());
    }
}
