package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.persistence.entity.BlogPostEntity;
import tn.temporise.infrastructure.persistence.entity.ImageBlogPostEntity;
import tn.temporise.infrastructure.persistence.entity.UtilisateurEntity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {DateMapper.class, ImageBlogMapper.class})
public interface BlogPostMapper {

  @Mapping(target = "user", source = "auteur", qualifiedByName = "mapUtilisateurIdToUtilisateur")

  BlogPost dtoToModel(ArticleRequest articleRequest);

  @Mapping(target = "auteur", source = "user")
  @Mapping(target = "status", source = "status")

  BlogPostEntity modelToEntity(BlogPost blogPost);

  @Mapping(target = "user", source = "auteur")
  @Mapping(target = "status", source = "status")
  @Mapping(target = "image", source = "images")

  BlogPost entityToModel(BlogPostEntity blogPost);

  @Mapping(target = "image", source = "image")
  @Mapping(target = "datePublication", source = "date_publication")
  ArticleResponse modelToResponse(BlogPost blogPost);

  @Mapping(target = "auteur", source = "auteur", qualifiedByName = "mapAuteurToString")


  ArticleResponse entityToResponse(BlogPostEntity blogPost);

  @Named("mapUtilisateurIdToUtilisateur")
  default UtilisateurModel mapUtilisateurIdToUtilisateur(Long id) {
    if (id == null)
      return null;
    return UtilisateurModel.builder().id(id).build();
  }

  @Named("mapAuteurToString")
  default String mapAuteurToString(UtilisateurEntity auteur) {
    if (auteur == null)
      return null;
    return auteur.getNom();
  }

  @Named("imagetoimageid")
  default List<Long> imagetoimageid(List<ImageBlogPost> imageBlogPosts) {
    if (imageBlogPosts == null || imageBlogPosts.isEmpty()) {
      return Collections.emptyList();
    }
    return imageBlogPosts.stream().map(ImageBlogPost::id).collect(Collectors.toList());
  }

  default List<ImageBlogPost> mapimageenitytoimage(
      List<ImageBlogPostEntity> imageBlogPostEntities) {
    if (imageBlogPostEntities == null || imageBlogPostEntities.isEmpty()) {
      return Collections.emptyList();
    }
    return imageBlogPostEntities.stream()
        .map(lc -> ImageBlogPost.builder().id(lc.getId()).url(lc.getUrl()).build()).toList();
  }

  default List<ArticleResponseImageInner> mapImageBlogPostToArticleResponseImageInner(
      List<ImageBlogPost> imageBlogPosts) {
    return imageBlogPosts.stream().map(lc -> {
      ArticleResponseImageInner articleResponseImageInner = new ArticleResponseImageInner();
      articleResponseImageInner.setId(lc.id());
      articleResponseImageInner.setUrl(lc.url());
      return articleResponseImageInner;
    }).toList();
  }
}
