package com.subastas.controller;

import com.subastas.dto.AbrirDisputaDTO;
import com.subastas.dto.ResolverDisputaDTO;
import com.subastas.entity.Disputa;
import com.subastas.entity.Usuario;
import com.subastas.service.DisputaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/disputas")
public class DisputaController {

    private final DisputaService disputaService;

    public DisputaController(DisputaService disputaService) {
        this.disputaService = disputaService;
    }

    @PostMapping
    public ResponseEntity<Disputa> abrirDisputa(
            @RequestParam Long subastaId,
            @Valid @RequestBody AbrirDisputaDTO dto,
            Authentication authentication
    ) {
        Usuario usuario = (Usuario) authentication.getPrincipal();

        return ResponseEntity.ok(
                disputaService.abrirDisputa(subastaId, usuario.getId(), dto)
        );
    }

    @PutMapping("/{id}/resolver")
    public ResponseEntity<Disputa> resolverDisputa(
            @PathVariable Long id,
            @Valid @RequestBody ResolverDisputaDTO dto,
            Authentication authentication
    ) {
        Usuario admin = (Usuario) authentication.getPrincipal();

        return ResponseEntity.ok(
                disputaService.resolverDisputa(id, admin.getId(), dto)
        );
    }

    @GetMapping
    public ResponseEntity<List<Disputa>> listar() {
        return ResponseEntity.ok(disputaService.listar());
    }
}