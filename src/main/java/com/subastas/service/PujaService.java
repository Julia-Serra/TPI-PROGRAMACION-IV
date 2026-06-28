package com.subastas.service;

import com.subastas.dto.PujaDTO;
import com.subastas.entity.Puja;
import com.subastas.entity.Subasta;
import com.subastas.entity.Usuario;
import com.subastas.enums.EstadoSubasta;
import com.subastas.repository.PujaRepository;
import com.subastas.repository.SubastaRepository;
import com.subastas.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class PujaService {

    private static final long MINUTOS_EXTENSION = 5;

    private final PujaRepository pujaRepository;
    private final SubastaRepository subastaRepository;
    private final UsuarioRepository usuarioRepository;

    public PujaService(PujaRepository pujaRepository,
                       SubastaRepository subastaRepository,
                       UsuarioRepository usuarioRepository) {
        this.pujaRepository = pujaRepository;
        this.subastaRepository = subastaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Puja realizarPuja(Long subastaId, Long compradorId, PujaDTO dto) {
        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la subasta indicada"));

        Usuario comprador = usuarioRepository.findById(compradorId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el comprador indicado"));

        LocalDateTime ahora = LocalDateTime.now();

        if (subasta.getEstado() != EstadoSubasta.ACTIVA) {
            throw new IllegalArgumentException("La subasta no esta activa");
        }

        if (!subasta.getFechaFin().isAfter(ahora)) {
            subasta.setEstado(EstadoSubasta.FINALIZADA);
            subastaRepository.save(subasta);
            throw new IllegalArgumentException("La subasta ya finalizo");
        }

        if (dto.monto().compareTo(subasta.getPrecioActual()) <= 0) {
            throw new IllegalArgumentException("La puja debe ser mayor al precio actual");
        }

        Puja puja = Puja.builder()
                .monto(dto.monto())
                .fechaHora(ahora)
                .comprador(comprador)
                .subasta(subasta)
                .build();

        subasta.setPrecioActual(dto.monto());
        extenderSiPujaEnUltimosCincoMinutos(subasta, ahora);

        subastaRepository.save(subasta);
        return pujaRepository.save(puja);
    }

    private void extenderSiPujaEnUltimosCincoMinutos(Subasta subasta, LocalDateTime ahora) {
        LocalDateTime inicioVentanaExtension = subasta.getFechaFin().minus(MINUTOS_EXTENSION, ChronoUnit.MINUTES);

        if (!ahora.isBefore(inicioVentanaExtension)) {
            subasta.setFechaFin(subasta.getFechaFin().plusMinutes(MINUTOS_EXTENSION));
        }
    }
}
