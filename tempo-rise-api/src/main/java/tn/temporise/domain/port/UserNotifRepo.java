package tn.temporise.domain.port;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.temporise.domain.model.UserNotif;

import java.util.List;

public interface UserNotifRepo {
  UserNotif save(UserNotif userNotif);

  List<UserNotif> saveAll(List<UserNotif> userNotifs);

  List<UserNotif> findByUserIdAndIsRead(Long userId, boolean isRead);

  Long countByUserIdAndIsRead(Long userId, boolean isRead);

  Page<UserNotif> findAll(Long userId, Pageable pageable);

  void markAllAsRead(Long userId);

  void markAsRead(Long userId, List<Long> notifIds, boolean isRead);


}
