package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import tn.temporise.domain.model.NotificationResponse;
import tn.temporise.domain.model.UserNotif;
import tn.temporise.domain.model.UserNotifResponse;
import tn.temporise.infrastructure.persistence.entity.UserNotifEntity;


import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {DateMapper.class, NotifMapper.class, UserMapper.class})
public interface UserNotifMapper {

  UserNotif entityToModel(UserNotifEntity userNotifEntity);

  UserNotifEntity modelToEntity(UserNotif userNotif);


  default UserNotifResponse modelToResponse(List<UserNotif> userNotif, NotifMapper notifMapper,
      DateMapper dateMapper, UserMapper userMapper) {
    if (userNotif == null || userNotif.isEmpty()) {
      return null;
    }
    UserNotifResponse userNotifResponse = new UserNotifResponse();
    userNotifResponse.setNotifs(userNotif.stream().map(userNotif1 -> {
      NotificationResponse notificationResponse = notifMapper.modelToResponse(userNotif1.notif());
      notificationResponse.setIsRead(userNotif1.read());
      notificationResponse.setReadAt(dateMapper.mapDateToOffsetDateTime(userNotif1.readAt()));
      return notificationResponse;
    }).collect(Collectors.toList()));
    userNotifResponse.setUser(userMapper.modelToResponse(userNotif.getFirst().user()));
    return userNotifResponse;
  }

}
