package tn.temporise.domain.model;

import lombok.Builder;

import java.time.LocalDateTime;
@Builder(toBuilder = true)
public record Contact(
        Long id,
        String subject,
        String message,
        TypeContact type,
        String email,
        StatusContact statusContact,
        RefundMethod refundMethod,
        Boolean isRefunded,
        LocalDateTime refundedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UtilisateurModel user,
        Commande commande
) {
    @Override
    public String toString() {
        return "Contact{" +
                "isRefunded=" + isRefunded +
                ", id=" + id +
                ", subject='" + subject + '\'' +
                ", message='" + message + '\'' +
                ", type=" + type +
                ", statusContact=" + statusContact +
                ", refundMethod=" + refundMethod +
                ", refundedAt=" + refundedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", user=" + user +
                ", commande=" + commande +
                '}';
    }
}
