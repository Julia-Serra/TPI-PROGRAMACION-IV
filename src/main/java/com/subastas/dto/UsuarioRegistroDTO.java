package com.subastas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioRegistroDTO(

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @Email(message = "Email inválido")
        @NotBlank(message = "El email es obligatorio")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password

) {
}