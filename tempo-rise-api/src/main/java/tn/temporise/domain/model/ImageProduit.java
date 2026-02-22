package tn.temporise.domain.model;


import lombok.Builder;

import java.util.Arrays;

@Builder(toBuilder = true)

public record ImageProduit(
        Long id,
        Produit produit,
        String url,
        byte[] contenu
) {

    @Override
    public String toString() {
        return "ImageProduit{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", contenu=" + Arrays.toString(contenu) +
                '}';
    }
}