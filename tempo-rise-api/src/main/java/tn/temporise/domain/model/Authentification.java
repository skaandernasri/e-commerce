package tn.temporise.domain.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record Authentification(
        Long id,
        String password,
        String providerId,
        String token,
        TypeAuthentification type,
        UtilisateurModel user
) {
    public Authentification(long id, String password,String providerId) {
        this(id, password, providerId,null,null,null);
    }

    public Authentification(UtilisateurModel utilisateurModel, String password, TypeAuthentification typeAuthentification, String providerId) {
        this(null, password, providerId,null,typeAuthentification,utilisateurModel);
    }

    @Override
    public String toString() {
        return "Authentification{" +
                "id=" + id +
                ", password='" + password + '\'' +
                ", providerId='" + providerId + '\'' +
                ", token='" + token + '\'' +
                ", type=" + type +
                ", user=" + user +
                '}';
    }
}
