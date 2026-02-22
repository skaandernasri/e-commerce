package tn.temporise.domain.model;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder(toBuilder=true)public record Section(
        Long id,String titre,TypeSection type,
        TypePage typePage,
        String imageUrl,
        String contenuJson,
        String contenuHtml,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UtilisateurModel createdBy,
        UtilisateurModel updatedBy
){
    @Override
    public String toString() {
        return "Section{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", type=" + type +
                ", typePage=" + typePage +
                ", contenuJson='" + contenuJson + '\'' +
                ", active='" + active + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy=" + createdBy +
                ", updatedBy=" + updatedBy +
                '}';
    }
}