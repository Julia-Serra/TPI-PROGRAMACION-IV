package com.subastas.controller;

import com.subastas.dto.PujaDTO;
import com.subastas.entity.Puja;
import com.subastas.repository.PujaRepository;
import com.subastas.service.PujaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pujas")
public class PujaController {

    private final PujaService pujaService;
    private final PujaRepository pujaRepository;

    public PujaController(PujaService pujaService, PujaRepository pujaRepository) {
        this.pujaService = pujaService;
        this.pujaRepository = pujaRepository;
    }

    @PostMapping
    public ResponseEntity<Puja> realizarPuja(
            @RequestParam Long subastaId,
            @Valid @RequestBody PujaDTO dto,
            Authentication authentication) {

        Usuario comprador = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(pujaService.realizarPuja(subastaId, comprador, dto));
    }

    @GetMapping("/subasta/{subastaId}")
    public ResponseEntity<List<Puja>> listarPorSubasta(@PathVariable Long subastaId) {
        return ResponseEntity.ok(pujaRepository.findBySubastaIdOrderByMontoDesc(subastaId));
    }

    @GetMapping("/usuario/{compradorId}")
    public ResponseEntity<List<Puja>> listarPorComprador(@PathVariable Long compradorId) {
        return ResponseEntity.ok(pujaRepository.findByCompradorIdOrderByFechaHoraDesc(compradorId));
    }
}