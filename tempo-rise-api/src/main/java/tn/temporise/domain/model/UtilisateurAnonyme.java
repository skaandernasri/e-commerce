package tn.temporise.domain.model;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;
@Builder(toBuilder = true)
public record UtilisateurAnonyme(
    Long id,
    UserType userType,
    String email,
    UUID sessionToken,
    LocalDateTime createdAt
) {
    @Override
    public String toString() {
        return "UtilisateurAnonyme{" +
                "id=" + id +
                ", sessionToken=" + sessionToken +
                ", created_at=" + createdAt +
                '}';
    }
}
