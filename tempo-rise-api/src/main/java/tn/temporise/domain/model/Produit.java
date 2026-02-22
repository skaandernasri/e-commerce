package tn.temporise.domain.model;


import lombok.Builder;

import java.util.Set;

@Builder(toBuilder=true)public record Produit(Long id,String nom,String description,double prix,String marque,Boolean actif,String composition,String guide,String faq,Set<Promotion>promotions,Categorie categorie,Set<ImageProduit>imageProduits,Set<Avis>avis,Set<Variant>variants){@Override public String toString(){return"Produit{"+"id="+id+", nom='"+nom+'\''+", description='"+description+'\''+", prix="+prix+", marque='"+marque+'\''+", composition='"+composition+'\''+", guide='"+guide+'\''+", faq='"+faq+'\''+", promotions="+promotions+", categorie="+categorie+", imageProduits="+imageProduits+", avis="+avis+", variants="+variants+'}';}}
