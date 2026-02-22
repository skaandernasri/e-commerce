package tn.temporise.domain.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record AvisFilter(String productName,String userEmail,String content){}
