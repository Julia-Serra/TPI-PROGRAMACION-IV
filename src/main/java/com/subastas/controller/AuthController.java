package com.subastas.controller;

import com.subastas.dto.LoginDTO;
import com.subastas.dto.UsuarioRegistroDTO;
import com.subastas.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public String registrar(@Valid @RequestBody UsuarioRegistroDTO dto) {
        return authService.registrar(dto);
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginDTO dto) {
        return authService.login(dto);
    }
}