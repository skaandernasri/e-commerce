package tn.temporise.infrastructure.persistence.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class UserNotifId implements Serializable {
  private Long user_id;
  private Long notification_id;

  public UserNotifId(Long user_id, Long notif_id) {
    this.user_id = user_id;
    this.notification_id = notif_id;
  }

  public UserNotifId() {}

  @Override
  public boolean equals(Object object) {
    if (object == null || getClass() != object.getClass())
      return false;
    UserNotifId that = (UserNotifId) object;
    return Objects.equals(user_id, that.user_id)
        && Objects.equals(notification_id, that.notification_id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user_id, notification_id);
  }

  @Override
  public String toString() {
    return "UserNotifId{" + "user_id=" + user_id + ", notif_id=" + notification_id + '}';
  }
}
