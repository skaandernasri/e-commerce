package tn.temporise.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.VariantMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.ProductRepo;
import tn.temporise.domain.port.VariantRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VariantService {
  private final VariantRepo variantRepo;
  private final VariantMapper variantMapper;
  private final ExceptionFactory exceptionFactory;
  private final ProductRepo productRepo;
  private final StockService stockService;

  public Response saveOrUpdateVariants(List<VariantRequest> variantRequest, Long produitId) {
    try {
      if (variantRequest == null || variantRequest.isEmpty()) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }

      List<Variant> variants = variantMapper.dtoToModel(variantRequest);

      // Properly update product reference on variants
      variants
          .replaceAll(v -> v.toBuilder().produit(Produit.builder().id(produitId).build()).build());

      // Validate existing variants with ID
      for (Variant v : variants) {
        if (v.id() != null) {
          variantRepo.findById(v.id());
        }
      }

      // Remove variants with null id that already exist (same color, size, product)
      variants.removeIf(v -> v.id() == null
          && variantRepo.existsByColorAndSizeAndProduitId(v.color(), v.size(), produitId));

      variantRepo.saveAll(variants);

      Response response = new Response();
      response.setMessage("Variants saved or updated");
      response.setCode("201");
      return response;

    } catch (NotFoundException | BadRequestException e) {
      log.warn("Variant creation failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Variant creation failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public Response deleteVariant(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }
      variantRepo.deleteById(id);
      Response response = new Response();
      response.setMessage("Variant deleted");
      response.setCode("200");
      return response;
    } catch (Exception e) {
      log.error("Variant deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public VariantResponse getVariant(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }
      Variant variant = variantRepo.findById(id);
      variant = variant.toBuilder().availableQuantity(stockService.getAvailableStock(variant))
          .reservedQuantity(stockService.getReservedQuantity(variant)).build();
      return variantMapper.modelToResponse(variant);
    } catch (NotFoundException | BadRequestException e) {
      log.warn("Variant deletion failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Variant deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public GetStockDetails200Response getStockDetails(Long stockMoreThan, Long stockLessThan,
      Long stockEquals) {
    try {
      log.info("variant stock details retrieval started");
      Long inStocks = variantRepo.countByQuantityreaterThan(stockMoreThan);
      Long lowStocks = variantRepo.countByQuantityLessThan(stockLessThan);
      Long outOfStocks = variantRepo.countByQuantityEquals(stockEquals);
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
}

