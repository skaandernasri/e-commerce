package tn.temporise.domain.port;

import tn.temporise.domain.model.BlogPost;



import java.util.List;


public interface BlogPostRepo {
  BlogPost save(BlogPost blogPost);

  BlogPost findById(Long id);

  List<BlogPost> findAll();

  BlogPost update(BlogPost blogPost);


  void deleteById(Long id);

  void deleteAll();



}
