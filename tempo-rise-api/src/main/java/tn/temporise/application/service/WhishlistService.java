package tn.temporise.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.WhishlistMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.ProductRepo;
import tn.temporise.domain.port.WhishlistItemRepo;
import tn.temporise.domain.port.WhishlistRepo;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class WhishlistService {
  private final WhishlistRepo whishlistRepo;
  private final WhishlistMapper whishlistMapper;
  private final WhishlistItemRepo whishlistItemRepo;
  private final ExceptionFactory exceptionFactory;
  private final ProductRepo productRepo;

  public WhishlistDto getWhishlist(Long userId) {
    try {
      if (userId == null)
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      Whishlist whishlist = whishlistRepo.findByUserId(userId);
      whishlist = enrishWhishlist(whishlist);
      return whishlistMapper.modelToDto(whishlist);
    } catch (BadRequestException | NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public Response addItemToWhishlist(Long userId, Long productId) {
    try {
      if (userId == null || productId == null)
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      Whishlist whishlist = whishlistRepo.findByUserId(userId);
      Produit produit = productRepo.findById(productId);
      if (whishlistItemRepo.existsByWhishlistIdAndProduitId(whishlist.id(), productId)) {
        throw exceptionFactory.badRequest("badrequest.item_already_in_whishlist");
      }
      WhishlistItem whishlistItem =
          WhishlistItem.builder().whishlist(whishlist).produit(produit).build();
      whishlistItemRepo.save(whishlistItem);
      Response response = new Response();
      response.setMessage("item added to whishlist");
      response.setCode("200");
      return response;
    } catch (BadRequestException | NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public Response removeItemFromWhishlist(Long userId, Long productId) {
    try {
      if (userId == null || productId == null)
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      whishlistItemRepo.removeItem(userId, productId);
      Response response = new Response();
      response.setMessage("item removed from whishlist");
      response.setCode("200");
      return response;
    } catch (BadRequestException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public Response removeAllItemsFromWhishlist(Long userId) {
    try {
      if (userId == null)
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      whishlistItemRepo.removeAllItems(userId);
      Response response = new Response();
      response.setMessage("all items removed from whishlist");
      response.setCode("200");
      return response;
    } catch (BadRequestException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  private Whishlist enrishWhishlist(Whishlist whishlist) {
    log.info("whichlist length : {}",
        whishlist.toBuilder()
            .produits(whishlistItemRepo.findByWhishlistId(whishlist.id()).stream()
                .map(WhishlistItem::produit).collect(Collectors.toSet()))
            .build().produits().size());
    return whishlist.toBuilder().produits(whishlistItemRepo.findByWhishlistId(whishlist.id())
        .stream().map(WhishlistItem::produit).collect(Collectors.toSet())).build();
  }

}
