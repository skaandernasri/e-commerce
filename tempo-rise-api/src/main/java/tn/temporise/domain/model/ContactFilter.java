package tn.temporise.domain.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record ContactFilter(
        TypeContact type,
        StatusContact statusContact,
        RefundMethod refundMethod)
{}

