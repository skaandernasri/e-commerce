package tn.temporise.domain.port;

import tn.temporise.domain.model.ImageBlogPost;

import java.util.List;

public interface ImageBlogRepo {
  ImageBlogPost save(ImageBlogPost imageBlogPost);

  ImageBlogPost findById(Long id);

  List<ImageBlogPost> findAll();

  ImageBlogPost update(ImageBlogPost imageBlogPost);

  void deleteById(Long id);

  void deleteAll();

  List<ImageBlogPost> findByProduitId(Long id);
}
