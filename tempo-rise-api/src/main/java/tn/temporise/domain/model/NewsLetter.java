package tn.temporise.domain.model;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder(toBuilder = true)
public record NewsLetter(
        Long id,
        String email,
        LocalDateTime createdAt,
        LocalDateTime subscribedAt,
        LocalDateTime unsubscribedAt,
        NewsLetterStatus status,
        UUID confirmationToken,
        LocalDateTime confirmationTokenExpiresAt,
        UUID unsubsriptionToken,
        LocalDateTime unsubsriptionTokenExpiresAt
) {
    @Override
    public String toString() {
        return "NewsLetter{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", subscribedAt=" + subscribedAt +
                ", unsubscribedAt=" + unsubscribedAt +
                ", status=" + status +
                ", confirmationToken=" + confirmationToken +
                ", confirmationTokenExpiry=" + confirmationTokenExpiresAt +
                ", unsubsriptionToken=" + unsubsriptionToken +
                ", unsubsriptionTokenExpiresAt=" + unsubsriptionTokenExpiresAt +
                '}';
    }
}
