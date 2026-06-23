package com.subastas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CrearSubastaDTO(

        @NotBlank(message = "El título es obligatorio")
        String titulo,

        @NotBlank(message = "La descripción es obligatoria")
        String descripcion,

        @NotNull(message = "El precio inicial es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
        BigDecimal precioInicial,

        @NotNull(message = "La fecha de fin es obligatoria")
        @Future(message = "La fecha de fin debe ser futura")
        LocalDateTime fechaFin

) {
}