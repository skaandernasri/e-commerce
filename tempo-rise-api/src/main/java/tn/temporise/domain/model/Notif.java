package tn.temporise.domain.model;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record Notif(
    Long id,
    String title,
    String message,
    NotifType type,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String redirectUrl
) {
    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", type=" + type +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", redirectUrl='" + redirectUrl + '\'' +
                '}';
    }
}
