package tn.temporise.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.ConflictException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.FactureMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.FactureRequest;
import tn.temporise.domain.model.FactureResponse;
import tn.temporise.domain.port.CommandeRepo;
import tn.temporise.domain.port.FactureRepo;
import tn.temporise.domain.port.UserRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactureService {
  private final FactureRepo factureRepo;
  private final FactureMapper factureMapper;
  private final ExceptionFactory exceptionFactory;
  private final UserRepo userRepo;
  private final CommandeRepo commandeRepo;

  /**
   * Retrieve all invoices
   *
   * @return List of all invoices
   */
  public List<FactureResponse> getAllFactures() {
    log.info("Fetching all invoices");
    return factureRepo.findAll().stream().map(factureMapper::modelToDto).toList();
  }

  /**
   * Create or update an invoice
   *
   * @param facture The invoice to save
   * @return The saved invoice
   */
  @Transactional
  public FactureResponse saveFacture(FactureRequest facture) {
    try {
      if (facture == null || facture.getCommandeId() == null || facture.getTotal() == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_request");
      }
      commandeRepo.findById(facture.getCommandeId());
      return factureMapper.modelToDto(factureRepo.save(factureMapper.dtoToModel(facture)));
    } catch (BadRequestException | ConflictException | NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  /**
   * Find an invoice by ID
   *
   * @param id The invoice ID
   * @return The found invoice
   * @throws RuntimeException if invoice not found
   */
  public FactureResponse getFactureById(Long id) {
    log.info("Fetching invoice with ID: {}", id);
    try {
      return factureMapper.modelToDto(factureRepo.findById(id)); // factureRepo.findById(id);
    } catch (NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  /**
   * Find invoices by user ID
   *
   * @param userId The user ID
   * @return List of invoices for the user
   */
  public List<FactureResponse> getFacturesByUserId(Long userId) {
    try {
      userRepo.findById(userId);
      log.info("Fetching invoices for user ID: {}", userId);
      return factureRepo.findByUtilisateurId(userId).stream().map(factureMapper::modelToDto)
          .toList();
    } catch (NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  /**
   * Find invoices by order ID
   *
   * @param commandeId The order ID
   * @return List of invoices for the order
   */
  public FactureResponse getFacturesByCommandeId(Long commandeId) {
    try {
      commandeRepo.findById(commandeId);
      log.info("Fetching invoices for order ID: {}", commandeId);
      return factureMapper.modelToDto(factureRepo.findByCommandeId(commandeId));
    } catch (NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  /**
   * Delete an invoice by ID
   *
   * @param id The invoice ID to delete
   */
  public void deleteFacture(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_request");
      }
      log.info("Deleting invoice with ID: {}", id);
      // First verify the invoice exists
      factureRepo.findById(id);
      factureRepo.deleteById(id);
    } catch (NotFoundException | BadRequestException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  /**
   * Delete all invoice
   */
  public void deleteAllFactures() {
    log.info("Deleting all invoices");
    factureRepo.deleteAll();
  }

}
