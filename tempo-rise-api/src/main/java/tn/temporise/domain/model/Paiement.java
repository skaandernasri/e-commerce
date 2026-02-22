package tn.temporise.domain.model;

import lombok.Builder;
import tn.temporise.domain.model.WebhookResponsePayment.StatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record Paiement(
    Long id,
    String paiementRef,
    LocalDateTime date,
    StatusEnum status,
    Commande commande,
    String nom,
    BigDecimal amount,
    String type
) {
    @Override
    public String toString() {
        return "Paiement{" +
                "id=" + id +
                ", paiementRef='" + paiementRef + '\'' +
                ", date=" + date +
                ", status='" + status + '\'' +
                ", commande=" + commande +
                ", nom='" + nom + '\'' +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                '}';
    }
}
