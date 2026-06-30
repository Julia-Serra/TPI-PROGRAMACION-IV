package com.subastas.service;

import com.subastas.dto.PujaDTO;
import com.subastas.entity.Puja;
import com.subastas.entity.Subasta;
import com.subastas.entity.Usuario;
import com.subastas.enums.EstadoSubasta;
import com.subastas.enums.RolUsuario;
import com.subastas.repository.PujaRepository;
import com.subastas.repository.SubastaRepository;
import com.subastas.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class PujaService {

    private static final long MINUTOS_EXTENSION = 5;

    private final PujaRepository pujaRepository;
    private final SubastaRepository subastaRepository;
    private final UsuarioRepository usuarioRepository;

    public PujaService(
            PujaRepository pujaRepository,
            SubastaRepository subastaRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.pujaRepository = pujaRepository;
        this.subastaRepository = subastaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Puja realizarPuja(Long subastaId, Long compradorId, PujaDTO dto) {

        Usuario comprador = usuarioRepository.findById(compradorId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el comprador indicado"));

        return realizarPuja(subastaId, comprador, dto);
    }

    @Transactional
    public Puja realizarPuja(Long subastaId, Usuario comprador, PujaDTO dto) {

        Subasta subasta = subastaRepository.findByIdForUpdate(subastaId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la subasta indicada"));

        LocalDateTime ahora = LocalDateTime.now(Clock.systemUTC());

        if (comprador.isBloqueado()) {
            throw new IllegalArgumentException("El usuario está bloqueado y no puede realizar pujas");
        }

        if (comprador.getRoles() == null || !comprador.getRoles().contains(RolUsuario.USER)) {
            throw new IllegalArgumentException("El usuario no tiene permiso para realizar pujas");
        }

        if (subasta.getVendedor().getId().equals(comprador.getId())) {
            throw new IllegalArgumentException("El vendedor no puede pujar en su propia subasta");
        }

        if (subasta.getEstado() != EstadoSubasta.ACTIVA) {
            throw new IllegalArgumentException("La subasta no está activa");
        }

        if (!subasta.getFechaFin().isAfter(ahora)) {
            finalizarOAdjudicar(subasta, ahora);
            throw new IllegalArgumentException("La subasta ya finalizó");
        }

        Puja ultimaPuja = pujaRepository.findTopBySubastaIdOrderByMontoDesc(subastaId)
                .orElse(null);

        BigDecimal montoMinimo;

        if (ultimaPuja == null) {
            montoMinimo = subasta.getPrecioInicial();
        } else {
            montoMinimo = ultimaPuja.getMonto().add(subasta.getIncrementoMinimo());
        }

        if (dto.monto().compareTo(montoMinimo) < 0) {
            throw new IllegalArgumentException("La puja mínima requerida es " + montoMinimo);
        }

        Puja puja = Puja.builder()
                .monto(dto.monto())
                .fechaHora(ahora)
                .comprador(comprador)
                .subasta(subasta)
                .build();

        subasta.setPrecioActual(dto.monto());
        subasta.setGanador(comprador);

        extenderSiPujaEnUltimosCincoMinutos(subasta, ahora);

        subastaRepository.save(subasta);

        return pujaRepository.save(puja);
    }

    private void finalizarOAdjudicar(Subasta subasta, LocalDateTime ahora) {

        boolean tienePujas = pujaRepository.existsBySubastaId(subasta.getId());

        if (tienePujas) {
            subasta.setEstado(EstadoSubasta.ADJUDICADA);
            subasta.setPrecioFinal(subasta.getPrecioActual());
            subasta.setFechaAdjudicacion(ahora);
        } else {
            subasta.setEstado(EstadoSubasta.FINALIZADA);
        }

        subastaRepository.save(subasta);
    }

    private void extenderSiPujaEnUltimosCincoMinutos(Subasta subasta, LocalDateTime ahora) {

        LocalDateTime inicioVentanaExtension =
                subasta.getFechaFin().minus(MINUTOS_EXTENSION, ChronoUnit.MINUTES);

        if (!ahora.isBefore(inicioVentanaExtension)) {
            subasta.setFechaFin(subasta.getFechaFin().plusMinutes(MINUTOS_EXTENSION));
        }
    }
}