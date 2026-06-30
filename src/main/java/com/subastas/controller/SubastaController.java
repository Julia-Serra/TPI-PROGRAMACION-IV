package com.subastas.controller;

import com.subastas.dto.CrearSubastaDTO;
import com.subastas.entity.Subasta;
import com.subastas.service.SubastaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.subastas.dto.CancelarSubastaDTO;

import java.util.List;

@RestController
@RequestMapping("/subastas")
public class SubastaController {

    private final SubastaService subastaService;

    public SubastaController(SubastaService subastaService) {
        this.subastaService = subastaService;
    }

    @GetMapping
    public List<Subasta> listar() {
        return subastaService.obtenerSubastasVisibles();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subasta> obtenerDetalle(@PathVariable Long id) {
        return subastaService.obtenerDetalle(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Subasta> crearSubasta(
            @Valid @RequestBody CrearSubastaDTO dto,
            @RequestParam Long vendedorId
    ) {
        return ResponseEntity.ok(subastaService.crearSubasta(dto, vendedorId));
    }

    @PostMapping("/{id}/publicar")
    public ResponseEntity<Subasta> publicar(
            @PathVariable Long id,
            @RequestParam Long vendedorId
    ) {
        return ResponseEntity.ok(subastaService.publicarSubasta(id, vendedorId));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Subasta> cancelar(
            @PathVariable Long id,
            @RequestParam Long usuarioId,
            @Valid @RequestBody CancelarSubastaDTO dto
    ) {
        return ResponseEntity.ok(
                subastaService.cancelarSubasta(id, usuarioId, dto)
        );
    }

    @PostMapping("/cerrar-vencidas")
    public ResponseEntity<String> cerrarSubastas() {
        int cantidad = subastaService.cerrarSubastasVencidas();
        return ResponseEntity.ok("Se cerraron " + cantidad + " subastas.");
    }
}