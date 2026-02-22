package tn.temporise.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.ConflictException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.application.mapper.ProductMapper;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.CategorieRepo;
import tn.temporise.domain.port.ProductRepo;
import tn.temporise.domain.port.VariantRepo;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
  private final ProductRepo productRepo;
  private final CategorieRepo categorieRepo;
  private final ProductMapper productMapper;
  private final ExceptionFactory exceptionFactory;
  private final PanierService panierService;
  private final VariantService variantService;
  private final VariantRepo variantRepo;
  private final ImageProductService imageProductService;
  private final StockService stockService;

  public ProductResponse createProduct(ProductRequest productRequest) {
    try {
      if (productRequest == null) {
        throw exceptionFactory.badRequest("invalid_request");
      }

      categorieRepo.findById(productRequest.getCategorie());
      Produit produit = productMapper.dtoToModel(productRequest);

      Produit savedProduit = productRepo.save(produit);
      variantService.saveOrUpdateVariants(productRequest.getVariants(), savedProduit.id());
      ProductResponse productResponse = productMapper.modelToResponse(savedProduit);
      productResponse.setVariants(productResponse.getVariants().stream().peek(variant -> {
        variant.availableQuantity(
            stockService.getAvailableStock(variantRepo.findById(variant.getId())));
        variant.reservedQuantity(
            stockService.getReservedQuantity(variantRepo.findById(variant.getId())));
      }).collect(Collectors.toList()));
      return productResponse;

    } catch (NullPointerException | BadRequestException | ConflictException | NotFoundException e) {
      log.warn("Product creation failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Product creation failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public ProductResponse getProductById(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      Produit produit = productRepo.findById(id);
      ProductResponse productResponse = productMapper.modelToResponse(produit);
      productResponse.setVariants(productResponse.getVariants().stream().peek(variant -> {
        variant.availableQuantity(
            stockService.getAvailableStock(variantRepo.findById(variant.getId())));
        variant.reservedQuantity(
            stockService.getReservedQuantity(variantRepo.findById(variant.getId())));
      }).collect(Collectors.toList()));
      // response.setQuantiteDisponible(getAvailableStock(produit));
      return productResponse;

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Product retrieval failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Product retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public List<ProductResponse> getAllProducts() {
    try {

      return productRepo.findAll().stream().map(products -> {
        ProductResponse productResponse = productMapper.modelToResponse(products);
        // productResponse.setQuantiteDisponible(getAvailableStock(products));
        productResponse.setVariants(productResponse.getVariants().stream().peek(variant -> {
          variant.availableQuantity(
              stockService.getAvailableStock(variantRepo.findById(variant.getId())));
          variant.reservedQuantity(
              stockService.getReservedQuantity(variantRepo.findById(variant.getId())));
        }).collect(Collectors.toList()));
        return productResponse;
      }).collect(Collectors.toList());

    } catch (NotFoundException e) {
      log.warn("No products found: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Product list retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  // public GetProductsPage200Response getAllPaginatedProducts(int number, int size) {
  // try {
  // Page<Produit> productPage = productRepo.findAll(PageRequest.of(number, size));
  // GetProductsPage200Response response = new GetProductsPage200Response();
  // response.setContent(productPage.getContent().stream().map(products -> {
  // ProductResponse productResponse = productMapper.modelToResponse(products);
  // productResponse.setVariants(productResponse.getVariants().stream().peek(variant -> {
  // variant.availableQuantity(
  // panierService.getAvailableStock(variantRepo.findById(variant.getId())));
  // variant.reservedQuantity(
  // panierService.getReservedQuantity(variantRepo.findById(variant.getId())));
  // }).collect(Collectors.toList()));
  // // productResponse.setQuantiteDisponible(getAvailableStock(products));
  // return productResponse;
  // }).collect(Collectors.toList()));
  // response.setTotalElements(productPage.getTotalElements());
  // response.setTotalPages(productPage.getTotalPages());
  // response.setCurrentPage(productPage.getNumber() + 1);
  // response.setSize(size);
  // log.info("Paginated Product list retrieved successfully : {}", response);
  // return response;
  // } catch (NotFoundException e) {
  // log.warn("No products found: {}", e.getMessage());
  // throw e;
  // } catch (Exception e) {
  // log.error("Product list retrieval failed unexpectedly", e);
  // throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
  // }
  // }

  public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
    try {
      log.info("updateProduct id: {}", id);
      log.info("updateProduct: {}", productRequest);
      if (id == null || productRequest == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }

      productRepo.findById(id);

      categorieRepo.findById(productRequest.getCategorie());
      Produit updatedProduit = productMapper.dtoToModel(productRequest);
      updatedProduit = updatedProduit.toBuilder().id(id).build();
      Produit savedProduit = productRepo.save(updatedProduit);
      log.info("products variants: {}", productRequest.getVariants());
      variantService.saveOrUpdateVariants(productRequest.getVariants(), savedProduit.id());
      ProductResponse productResponse = productMapper.modelToResponse(savedProduit);
      productResponse.setVariants(productResponse.getVariants().stream().peek(variant -> {
        variant.availableQuantity(
            stockService.getAvailableStock(variantRepo.findById(variant.getId())));
        variant.reservedQuantity(
            stockService.getReservedQuantity(variantRepo.findById(variant.getId())));
      }).collect(Collectors.toList()));
      return productResponse;

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Product update failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Product update failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public void deleteProduct(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      productRepo.findById(id);
      List<ImageProduitResponse> imageProduitResponses =
          imageProductService.getImageByProductId(id);
      imageProduitResponses.forEach(imageProduitResponse -> {
        imageProductService.deleteImage(imageProduitResponse.getId());
      });
      productRepo.deleteById(id);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Product deletion failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Product deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public void deleteAllProducts() {
    try {
      productRepo.findAll();
      productRepo.deleteAll();

    } catch (NotFoundException e) {
      log.warn("No products to delete: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Product bulk deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public List<ProductResponse> getProductsByIds(List<Long> ids) {
    try {
      if (ids.isEmpty()) {
        ProductResponse productResponse = new ProductResponse();
        return List.of(productResponse);
      }
      return productRepo.findByIds(ids, true).stream().map(products -> {
        ProductResponse productResponse = productMapper.modelToResponse(products);
        // productResponse.setQuantiteDisponible(getAvailableStock(products));
        productResponse.setVariants(productResponse.getVariants().stream().peek(variant -> {
          variant.availableQuantity(
              stockService.getAvailableStock(variantRepo.findById(variant.getId())));
          variant.reservedQuantity(
              stockService.getReservedQuantity(variantRepo.findById(variant.getId())));
        }).collect(Collectors.toList()));
        return productResponse;
      }).collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Product list retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public GetFilteredProductsPage200Response getAllProductPagedAndFiltered(int page, int size,
      String productName, List<String> categoryNames, Double minPrice, Double maxPrice,
      Double minPromotion, Double maxPromotion, Boolean actif, Boolean orderByRatingDesc) {
    try {
      log.info("Paginated Product list retrieval started{}", orderByRatingDesc);

      page = Math.max(page - 1, 0);
      ProduitFilter filter =
          ProduitFilter.builder().productName(productName).categoryNames(categoryNames)
              .minPrice(minPrice).maxPrice(maxPrice).orderByRatingDesc(orderByRatingDesc)
              .minPromotion(minPromotion).maxPromotion(maxPromotion).actif(actif).build();
      Page<Produit> productPage = productRepo.findAll(filter, PageRequest.of(page, size));
      GetFilteredProductsPage200Response response = new GetFilteredProductsPage200Response();
      response.setContent(productPage.getContent().stream().map(product -> {
        ProductResponse productResponse = productMapper.modelToResponse(product);
        productResponse.setVariants(productResponse.getVariants().stream().peek(variant -> {
          variant.availableQuantity(
              stockService.getAvailableStock(variantRepo.findById(variant.getId())));
          variant.reservedQuantity(
              stockService.getReservedQuantity(variantRepo.findById(variant.getId())));
        }).collect(Collectors.toList()));
        // productResponse.setQuantiteDisponible(getAvailableStock(product));
        return productResponse;
      }).collect(Collectors.toList()));
      response.setTotalElements(productPage.getTotalElements());
      response.setTotalPages(productPage.getTotalPages());
      response.setCurrentPage(productPage.getNumber() + 1);
      response.setMaxPrice(productRepo.getMaxPrice());
      log.info("Paginated Product list retrieved successfully : {}", response);
      return response;
    } catch (NotFoundException e) {
      log.warn("No products found: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Product list retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public List<ProductResponse> getFilteredProducts(String productName, List<String> categoryNames,
      Boolean actif) {
    try {
      ProduitFilter filter = ProduitFilter.builder().productName(productName).actif(actif)
          .categoryNames(categoryNames).build();
      return productRepo.filteredProducts(filter).stream().map(product -> {
        ProductResponse productResponse = productMapper.modelToResponse(product);
        productResponse.setVariants(productResponse.getVariants().stream().peek(variant -> {
          variant.availableQuantity(
              stockService.getAvailableStock(variantRepo.findById(variant.getId())));
          variant.reservedQuantity(
              stockService.getReservedQuantity(variantRepo.findById(variant.getId())));
          variant.quantity(variantRepo.findById(variant.getId()).quantity());
        }).collect(Collectors.toList()));
        // productResponse.setQuantiteDisponible(getAvailableStock(product));
        return productResponse;
      }).collect(Collectors.toList());
    } catch (NotFoundException e) {
      log.warn("No products found: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Product list retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }

  }

  public GetStockDetails200Response getStockDetails(Long stockMoreThan, Long stockLessThan,
      Long stockEquals) {
    try {
      log.info("Product stock details retrieval started");
      Long inStocks = productRepo.countProductsWithTotalQuantityGreaterThan(stockMoreThan);
      Long lowStocks = productRepo.countProductsWithTotalQuantityLessThanEqual(stockLessThan);
      Long outOfStocks = productRepo.countProductsWithTotalQuantityEquals(stockEquals);
      Long totalProducts = productRepo.countAll();
      GetStockDetails200Response response = new GetStockDetails200Response();
      response.setInStock(inStocks);
      response.setLowStock(lowStocks);
      response.setOutOfStock(outOfStocks);
      response.setTotalProducts(totalProducts);
      log.info("Product stock details retrieved successfully : {}", response);
      return response;
    } catch (NotFoundException e) {
      log.warn("No products found: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Product stock details retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  // private void setStock(ProductRequest productRequest) {
  // if (productRequest.getQuantite() > 0)
  // productRequest.setStock(productRequest.getStock() + productRequest.getQuantite());
  // }

  // private int getAvailableStock(Produit produit) {
  // AtomicInteger stock = new AtomicInteger(produit.stock());
  // panierVariantRepo.findByVariantId(produit.id())
  // .forEach(panierProduit -> stock.addAndGet(-panierProduit.quantite()));
  // return stock.get();
  // }
}
