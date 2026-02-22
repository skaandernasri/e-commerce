package tn.temporise.domain.model;

import lombok.Builder;
import tn.temporise.infrastructure.persistence.entity.UserNotifId;

import java.time.LocalDateTime;

@Builder(toBuilder = true)

public record UserNotif (
    UserNotifId id,
    Notif notif,
    UtilisateurModel user,
    boolean read,
    LocalDateTime readAt
) {
    @Override
    public String toString() {
        return "UserNotif{" +
                "id=" + id +
                ", notif=" + notif +
                ", user=" + user +
                ", isRead=" + read +
                ", readAt=" + readAt +
                '}';
    }
}
