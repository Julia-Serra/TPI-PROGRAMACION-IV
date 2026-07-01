package com.subastas.controller;

import com.subastas.dto.PujaDTO;
import com.subastas.entity.Puja;
import com.subastas.entity.Usuario;
import com.subastas.repository.PujaRepository;
import com.subastas.service.PujaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.subastas.dto.PujaPublicaDTO;

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
    public ResponseEntity<List<PujaPublicaDTO>> listarPorSubasta(@PathVariable Long subastaId) {
        List<PujaPublicaDTO> pujas = pujaRepository.findBySubastaIdOrderByMontoDesc(subastaId)
                .stream()
                .map(puja -> new PujaPublicaDTO(
                        puja.getMonto(),
                        puja.getFechaHora(),
                        "Oferente #" + puja.getComprador().getId()
                ))
                .toList();

        return ResponseEntity.ok(pujas);
    }

   @GetMapping("/mis-pujas")
    public ResponseEntity<List<PujaPublicaDTO>> listarMisPujas(Authentication authentication) {

        Usuario comprador = (Usuario) authentication.getPrincipal();

        List<PujaPublicaDTO> pujas = pujaRepository
                .findByCompradorIdOrderByFechaHoraDesc(comprador.getId())
                .stream()
                .map(puja -> new PujaPublicaDTO(
                        puja.getMonto(),
                        puja.getFechaHora(),
                        "Mi puja"
                ))
                .toList();

        return ResponseEntity.ok(pujas);
    }
}