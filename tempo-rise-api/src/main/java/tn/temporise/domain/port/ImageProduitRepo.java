package tn.temporise.domain.port;

import tn.temporise.domain.model.ImageProduit;

import java.util.List;

public interface ImageProduitRepo {
  ImageProduit save(ImageProduit imageProduit);

  ImageProduit findById(Long id);

  List<ImageProduit> findAll();

  ImageProduit update(ImageProduit imageProduit);

  void deleteById(Long id);

  void deleteAll();

  List<ImageProduit> findByProduitId(Long id);
}
