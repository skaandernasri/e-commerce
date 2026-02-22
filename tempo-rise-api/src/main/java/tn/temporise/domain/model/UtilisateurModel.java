package tn.temporise.domain.model;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
@Builder(toBuilder = true)
public record UtilisateurModel(
        Long id,
        String nom,
        String prenom,
        String telephone,
        String email,
        String password,
        Date date_naissance,
        Genre genre,
        UserType userType,
        int loyalty_group,
        byte[] image,
        Set<Role> roles,
        boolean isverified,
        String activation_token,
        String resetpasswordtoken,
        LocalDateTime resetpasswordexpiresat,
        LocalDateTime activationtokenexpiresat,
        String imageUrl
) {
    @Override
    public String toString() {
        return "UtilisateurModel{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", telephone='" + telephone + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", date_naissance=" + date_naissance +
                ", genre=" + genre +
                ", loyalty_group=" + loyalty_group +
                ", image=" + Arrays.toString(image) +
                ", roles=" + roles +
                ", isverified=" + isverified +
                ", activation_token='" + activation_token + '\'' +
                ", resetpasswordtoken='" + resetpasswordtoken + '\'' +
                ", resetpasswordexpiresat=" + resetpasswordexpiresat +
                ", activationtokenexpiresat=" + activationtokenexpiresat +
                '}';
    }
}
