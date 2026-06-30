package com.subastas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PujaPublicaDTO(
        BigDecimal monto,
        LocalDateTime fechaHora,
        String oferente
) {}