package tn.temporise.domain.model;


import lombok.Builder;

import java.time.LocalDateTime;

@Builder

public record CodePromo(
        Long id,
        String code,
        double reduction,
        LocalDateTime dateExpiration
) {}