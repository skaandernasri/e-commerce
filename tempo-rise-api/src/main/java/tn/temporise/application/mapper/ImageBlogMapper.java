package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.web.multipart.MultipartFile;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.persistence.entity.ImageBlogPostEntity;
import tn.temporise.infrastructure.persistence.entity.BlogPostEntity;

import java.io.IOException;
import java.util.Base64;

@Mapper(componentModel = "spring")
public interface ImageBlogMapper {

  // Mapping de l'entité vers le modèle de domaine
  @Mapping(target = "blogPost", source = "blogPost",
      qualifiedByName = "mapBlogPostEntityToBlogPost")
  ImageBlogPost entityToModel(ImageBlogPostEntity entity);

  // Mapping du DTO vers le modèle de domaine avec conversion de blogPostId en BlogPost
  @Mapping(target = "blogPost", source = "blogPostId", qualifiedByName = "mapIdToBlogPost")
  ImageBlogPost dtoToModel(MultipartFile image, Long blogPostId);

  // Mapping du modèle de domaine vers l'entité
  // @Mapping(target = "blogPost", source = "blogPost", qualifiedByName =
  // "mapBlogPostToBlogPostEntity")
  ImageBlogPostEntity modelToEntity(ImageBlogPost model);

  // Mapping du modèle de domaine vers la réponse DTO
  ImageBlogPostResponse modelToResponse(ImageBlogPost imageBlogPost);


  // Convertit base64 String en byte[]
  default byte[] mapBase64ToBytes(MultipartFile image) throws IOException {
    if (image == null || image.isEmpty())
      return null;
    return image.getBytes();
  }

  // Convertit byte[] en base64 String
  @Named("mapBytesToBase64")
  default String mapBytesToBase64(byte[] image) {
    if (image == null || image.length == 0)
      return null;
    return Base64.getEncoder().encodeToString(image);
  }

  // Mapping BlogPostEntity → BlogPost
  @Named("mapBlogPostEntityToBlogPost")
  default BlogPost mapBlogPostEntityToBlogPost(BlogPostEntity entity) {
    if (entity == null) {
      return null;
    }
    return BlogPost.builder().id(entity.getId())
        // Ajouter d'autres champs si nécessaire, ex: titre, description
        .build();
  }

  // Mapping BlogPost → BlogPostEntity
  @Named("mapBlogPostToBlogPostEntity")
  default BlogPostEntity mapBlogPostToBlogPostEntity(BlogPost blogPost) {
    if (blogPost == null) {
      return null;
    }
    BlogPostEntity entity = new BlogPostEntity();
    entity.setId(blogPost.id());
    // Ajouter d'autres champs si nécessaire
    return entity;
  }

  // Conversion d'un ID Long en BlogPost (pour mapping DTO)
  @Named("mapIdToBlogPost")
  default BlogPost mapIdToBlogPost(Long blogPostId) {
    if (blogPostId == null) {
      return null;
    }
    return BlogPost.builder().id(blogPostId).build();
  }


}
