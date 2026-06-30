package com.subastas.dto;

import com.subastas.enums.EstadoSubasta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResolverDisputaDTO(
        @NotNull EstadoSubasta estadoFinal,
        @NotBlank String resolucion
) {}