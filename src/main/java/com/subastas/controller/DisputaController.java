package com.subastas.controller;

import com.subastas.dto.AbrirDisputaDTO;
import com.subastas.dto.ResolverDisputaDTO;
import com.subastas.entity.Disputa;
import com.subastas.service.DisputaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
            @RequestParam Long usuarioId,
            @Valid @RequestBody AbrirDisputaDTO dto
    ) {
        return ResponseEntity.ok(disputaService.abrirDisputa(subastaId, usuarioId, dto));
    }

    @PutMapping("/{id}/resolver")
    public ResponseEntity<Disputa> resolverDisputa(
            @PathVariable Long id,
            @RequestParam Long adminId,
            @Valid @RequestBody ResolverDisputaDTO dto
    ) {
        return ResponseEntity.ok(disputaService.resolverDisputa(id, adminId, dto));
    }

    @GetMapping
    public ResponseEntity<List<Disputa>> listar() {
        return ResponseEntity.ok(disputaService.listar());
    }
}