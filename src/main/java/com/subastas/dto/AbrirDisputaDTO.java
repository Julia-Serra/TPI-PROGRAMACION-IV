package com.subastas.dto;

import jakarta.validation.constraints.NotBlank;

public record AbrirDisputaDTO(
        @NotBlank String motivo,
        String descripcion
) {}