package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.UserNotifMapper;
import tn.temporise.domain.model.UserNotif;
import tn.temporise.domain.port.UserNotifRepo;
import tn.temporise.infrastructure.persistence.entity.UserNotifId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Repository
public class UserNotifRepoImp implements UserNotifRepo {
  private final UserNotifMapper userNotifMapper;
  private final UserNotifJpaRepo userNotifJpaRepo;

  @Override
  public UserNotif save(UserNotif userNotif) {
    return userNotifMapper
        .entityToModel(userNotifJpaRepo.save(userNotifMapper.modelToEntity(userNotif)));
  }

  @Override
  public List<UserNotif> saveAll(List<UserNotif> userNotifs) {
    return userNotifJpaRepo
        .saveAll(
            userNotifs.stream().map(userNotifMapper::modelToEntity).collect(Collectors.toList()))
        .stream().map(userNotifMapper::entityToModel).collect(Collectors.toList());
  }

  @Override
  public List<UserNotif> findByUserIdAndIsRead(Long userId, boolean isRead) {
    return userNotifJpaRepo.findByUser_IdAndRead(userId, isRead).stream()
        .map(userNotifMapper::entityToModel).collect(Collectors.toList());
  }

  @Override
  public Long countByUserIdAndIsRead(Long userId, boolean isRead) {
    return userNotifJpaRepo.countByUser_IdAndRead(userId, isRead);
  }

  @Override
  public Page<UserNotif> findAll(Long userId, Pageable pageable) {
    return userNotifJpaRepo.findByUser_Id(userId, pageable).map(userNotifMapper::entityToModel);
  }

  @Override
  public void markAllAsRead(Long userId) {
    userNotifJpaRepo.findByUser_IdAndRead(userId, false).forEach(userNotif -> {
      userNotif.setRead(true);
      userNotif.setReadAt(LocalDateTime.now());
      userNotifJpaRepo.save(userNotif);
    });
  }

  @Override
  public void markAsRead(Long userId, List<Long> notifIds, boolean isRead) {
    List<UserNotifId> userNotifsId = userNotifJpaRepo
        .findByUser_IdAndNotif_IdInAndRead(userId, notifIds, isRead).stream()
        .map(notif -> new UserNotifId(notif.getUser().getId(), notif.getNotif().getId())).toList();
    userNotifJpaRepo.findAllById(userNotifsId).forEach(userNotif -> {
      userNotif.setRead(true);
      userNotif.setReadAt(LocalDateTime.now());
      userNotifJpaRepo.save(userNotif);
    });
  }
}
