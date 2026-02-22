package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.CommandeMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.Commande;
import tn.temporise.domain.model.CommandeFilter;
import tn.temporise.domain.model.StatutCommande;
import tn.temporise.domain.model.UserType;
import tn.temporise.domain.port.CommandeRepo;
import tn.temporise.infrastructure.persistence.entity.CommandeEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class CommandeRepoImp implements CommandeRepo {
  private final CommandeJpaRepo commandeJpaRepo;
  private final CommandeMapper commandeMapper;
  private final ExceptionFactory exceptionFactory;

  @Override
  public Commande save(Commande commande) {
    CommandeEntity commandeEntity = commandeJpaRepo.save(commandeMapper.modelToEntity(commande));
    log.info("CommandeEntity saved: {}", commandeEntity);
    return commandeMapper.entityToModel(commandeEntity);
  }

  @Override
  public Commande findById(Long id) {
    CommandeEntity commandeEntity = commandeJpaRepo.findByIdWithLignes(id)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.commande"));
    return commandeMapper.entityToModel(commandeEntity);
  }

  @Override
  public List<Commande> findAll() {
    List<CommandeEntity> commandeEntities = commandeJpaRepo.findAllWithLignes();
    log.info("commandeEntities: {}", commandeEntities);
    return commandeEntities.stream().map(commandeMapper::entityToModel).toList();
  }

  @Override
  public Page<Commande> findAll(CommandeFilter filter, Pageable pageable) {
    Specification<CommandeEntity> spec = Specification.where(null);
    if (filter.status() != null) {
      spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("statut"),
          filter.status().name()));
      log.info("filter status: {}", filter.status().name());

    }
    if (filter.email() != null) {
      spec = spec.and(((root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("email"),
          "%" + filter.email() + "%")));
      log.info("email filter", filter.email());

    }
    log.info("spec: {}", spec);

    return commandeJpaRepo.findAll(spec, pageable).map(commandeMapper::entityToModel);
  }

  @Override
  public Double findTotalCommandes() {
    return commandeJpaRepo.findTotalCommandes();
  }

  @Override
  public Double findTotalCommandesByStatut(StatutCommande statut) {
    return commandeJpaRepo.findTotalCommandesByStatut(statut);
  }

  @Override
  public Long countCommandes() {
    return commandeJpaRepo.countCommandes();
  }


  @Override
  public void deleteById(Long id) {
    findById(id);
    commandeJpaRepo.deleteById(id);
  }

  @Override
  public void deleteAll() {
    commandeJpaRepo.deleteAll();
  }

  @Override
  public List<Commande> findByUserId(Long id) {
    List<CommandeEntity> commandeEntities = commandeJpaRepo.findByUserIdWithLignes(id)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.no_commandes"));
    return commandeEntities.stream().map(commandeMapper::entityToModel).toList();
  }

  @Override
  public List<Commande> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
    return commandeJpaRepo.findAllByDateBetween(startDate, endDate).stream()
        .map(commandeMapper::entityToModel).collect(Collectors.toList());
  }

  @Override
  public Commande findByCodePromoId(Long id) {
    CommandeEntity commandeEntity = commandeJpaRepo.findByCodePromoId(id)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.commande"));
    return commandeMapper.entityToModel(commandeEntity);
  }

  @Override
  public boolean existsByIdAndUserId(Long id, Long userId) {
    return commandeJpaRepo.existsByIdAndUser_Id(id, userId);
  }

  @Override
  public boolean existsByIdAndUser_IdAndUserType(Long id, Long userId, UserType userType) {
    return commandeJpaRepo.existsByIdAndUser_IdAndUser_UserType(id, userId, userType);
  }

  @Override
  public List<Commande> findByAdresseFacturationId(Long id) {
    return commandeJpaRepo.findByAdresseFacturation_Id(id).stream()
        .map(commandeMapper::entityToModel).toList();
  }

  @Override
  public List<Commande> findByAdresseLivraisonId(Long id) {
    return commandeJpaRepo.findByAdresseLivraison_Id(id).stream().map(commandeMapper::entityToModel)
        .toList();

  }


}
