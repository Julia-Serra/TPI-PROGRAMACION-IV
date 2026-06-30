package com.subastas.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelarSubastaDTO(
        @NotBlank String motivo
) {}