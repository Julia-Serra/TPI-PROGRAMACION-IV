package com.subastas.service;

import com.subastas.entity.HistorialEstadoSubasta;
import com.subastas.entity.Subasta;
import com.subastas.entity.Usuario;
import com.subastas.enums.EstadoSubasta;
import com.subastas.repository.HistorialEstadoSubastaRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistorialEstadoSubastaService {

    private final HistorialEstadoSubastaRepository historialRepository;

    public HistorialEstadoSubastaService(HistorialEstadoSubastaRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    public void registrarCambio(
            Subasta subasta,
            EstadoSubasta estadoAnterior,
            EstadoSubasta estadoNuevo,
            Usuario usuarioResponsable,
            String motivo
    ) {
        HistorialEstadoSubasta historial = HistorialEstadoSubasta.builder()
                .subasta(subasta)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .usuarioResponsable(usuarioResponsable)
                .motivo(motivo)
                .fecha(LocalDateTime.now(Clock.systemUTC()))
                .build();

        historialRepository.save(historial);
    }

    public List<HistorialEstadoSubasta> listarPorSubasta(Long subastaId) {
        return historialRepository.findBySubastaIdOrderByFechaDesc(subastaId);
    }
}