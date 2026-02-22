package tn.temporise.application.mapper;

import org.mapstruct.Mapper;

import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.persistence.entity.NotifEntity;



@Mapper(componentModel = "spring", uses = {DateMapper.class})
public interface NotifMapper {

  NotifEntity modelToEntity(Notif model);

  Notif entityToModel(NotifEntity entity);

  NotificationResponse modelToResponse(Notif model);

}
