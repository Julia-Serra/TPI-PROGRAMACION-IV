package com.subastas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CrearSubastaDTO(

        @NotNull
        Long productoId,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal precioBase,

        @NotNull
        LocalDateTime fechaInicio,

        @NotNull
        LocalDateTime fechaCierre,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal incrementoMinimo

) {}