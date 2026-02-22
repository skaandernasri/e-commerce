package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.NotifMapper;
import tn.temporise.domain.model.Notif;
import tn.temporise.domain.port.NotifRepo;


@Repository
@Slf4j
@RequiredArgsConstructor
public class NotifRepoImp implements NotifRepo {
  private final NotifJpaRepo notifJpaRepo;
  private final NotifMapper notifMapper;

  @Override
  public Notif save(Notif notif) {
    return notifMapper.entityToModel(notifJpaRepo.save(notifMapper.modelToEntity(notif)));
  }

}
