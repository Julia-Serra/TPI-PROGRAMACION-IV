package com.subastas.controller;

import org.springframework.security.core.Authentication;
import com.subastas.entity.Usuario;
import com.subastas.enums.RolUsuario;
import com.subastas.repository.UsuarioRepository;
import com.subastas.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public UsuarioController(
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @GetMapping("/me")
    public ResponseEntity<Usuario> obtenerUsuarioActual(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/{id}/bloquear")
    public ResponseEntity<Usuario> bloquear(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.bloquear(id));
    }

    @PutMapping("/{id}/desbloquear")
    public ResponseEntity<Usuario> desbloquear(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.desbloquear(id));
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<Usuario> actualizarRoles(
            @PathVariable Long id,
            @RequestBody Set<RolUsuario> roles
    ) {
        return ResponseEntity.ok(usuarioService.actualizarRoles(id, roles));
    }
}