package tn.temporise.infrastructure.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.temporise.infrastructure.persistence.entity.UserNotifEntity;
import tn.temporise.infrastructure.persistence.entity.UserNotifId;

import java.util.List;

public interface UserNotifJpaRepo extends JpaRepository<UserNotifEntity, UserNotifId> {
  List<UserNotifEntity> findByUser_IdAndRead(Long userId, boolean isRead);

  Long countByUser_IdAndRead(Long userId, boolean isRead);

  Page<UserNotifEntity> findByUser_Id(Long userId, Pageable pageable);

  List<UserNotifEntity> findByUser_IdAndNotif_IdInAndRead(Long userId, List<Long> notifIds,
      boolean isRead);

}
