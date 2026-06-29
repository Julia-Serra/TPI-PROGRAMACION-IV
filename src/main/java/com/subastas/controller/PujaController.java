package com.subastas.controller;

import com.subastas.dto.PujaDTO;
import com.subastas.entity.Puja;
import com.subastas.service.PujaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pujas")
public class PujaController {

    private final PujaService pujaService;

    public PujaController(PujaService pujaService) {
        this.pujaService = pujaService;
    }

    @PostMapping
    public ResponseEntity<Puja> realizarPuja(
            @RequestParam Long subastaId,
            @RequestParam Long compradorId,
            @Valid @RequestBody PujaDTO dto) {

        return ResponseEntity.ok(pujaService.realizarPuja(subastaId, compradorId, dto));
    }
}