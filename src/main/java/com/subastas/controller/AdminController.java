package com.subastas.controller;

import com.subastas.dto.CancelarSubastaDTO;
import com.subastas.dto.ResolverDisputaDTO;
import com.subastas.entity.Disputa;
import com.subastas.entity.Producto;
import com.subastas.entity.Subasta;
import com.subastas.entity.Usuario;
import com.subastas.enums.RolUsuario;
import com.subastas.repository.DisputaRepository;
import com.subastas.repository.ProductoRepository;
import com.subastas.repository.SubastaRepository;
import com.subastas.repository.UsuarioRepository;
import com.subastas.service.DisputaService;
import com.subastas.service.SubastaService;
import com.subastas.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final SubastaRepository subastaRepository;
    private final DisputaRepository disputaRepository;
    private final UsuarioService usuarioService;
    private final SubastaService subastaService;
    private final DisputaService disputaService;

    public AdminController(
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository,
            SubastaRepository subastaRepository,
            DisputaRepository disputaRepository,
            UsuarioService usuarioService,
            SubastaService subastaService,
            DisputaService disputaService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.subastaRepository = subastaRepository;
        this.disputaRepository = disputaRepository;
        this.usuarioService = usuarioService;
        this.subastaService = subastaService;
        this.disputaService = disputaService;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @PutMapping("/usuarios/{email}/roles")
    public ResponseEntity<Usuario> asignarRolesPorEmail(
            @PathVariable String email,
            @RequestBody Set<RolUsuario> roles
    ) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No existe un usuario con ese email"));

        return ResponseEntity.ok(usuarioService.actualizarRoles(usuario.getId(), roles));
    }

    @PutMapping("/usuarios/{email}/suspender")
    public ResponseEntity<Usuario> suspenderUsuario(@PathVariable String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No existe un usuario con ese email"));

        return ResponseEntity.ok(usuarioService.bloquear(usuario.getId()));
    }

    @PutMapping("/usuarios/{email}/reactivar")
    public ResponseEntity<Usuario> reactivarUsuario(@PathVariable String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No existe un usuario con ese email"));

        return ResponseEntity.ok(usuarioService.desbloquear(usuario.getId()));
    }

    @GetMapping("/subastas")
    public ResponseEntity<List<Subasta>> listarSubastas() {
        return ResponseEntity.ok(subastaRepository.findAll());
    }

    @PutMapping("/subastas/{id}/cancelar")
    public ResponseEntity<Subasta> cancelarSubasta(
            @PathVariable Long id,
            @RequestParam Long adminId,
            @Valid @RequestBody CancelarSubastaDTO dto
    ) {
        return ResponseEntity.ok(subastaService.cancelarSubasta(id, adminId, dto));
    }

    @GetMapping("/disputas")
    public ResponseEntity<List<Disputa>> listarDisputas() {
        return ResponseEntity.ok(disputaRepository.findAll());
    }

    @PutMapping("/disputas/{id}/resolver")
    public ResponseEntity<Disputa> resolverDisputa(
            @PathVariable Long id,
            @RequestParam Long adminId,
            @Valid @RequestBody ResolverDisputaDTO dto
    ) {
        return ResponseEntity.ok(disputaService.resolverDisputa(id, adminId, dto));
    }
}