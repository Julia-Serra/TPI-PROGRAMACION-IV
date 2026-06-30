package com.subastas.service;

import com.subastas.dto.AbrirDisputaDTO;
import com.subastas.dto.ResolverDisputaDTO;
import com.subastas.entity.Disputa;
import com.subastas.entity.Subasta;
import com.subastas.entity.Usuario;
import com.subastas.enums.EstadoSubasta;
import com.subastas.enums.RolUsuario;
import com.subastas.repository.DisputaRepository;
import com.subastas.repository.SubastaRepository;
import com.subastas.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DisputaService {

    private final DisputaRepository disputaRepository;
    private final SubastaRepository subastaRepository;
    private final UsuarioRepository usuarioRepository;

    public DisputaService(
            DisputaRepository disputaRepository,
            SubastaRepository subastaRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.disputaRepository = disputaRepository;
        this.subastaRepository = subastaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Disputa abrirDisputa(Long subastaId, Long usuarioId, AbrirDisputaDTO dto) {
        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la subasta indicada"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el usuario indicado"));

        if (subasta.getEstado() != EstadoSubasta.ADJUDICADA) {
            throw new IllegalArgumentException("Solo se puede abrir disputa sobre una subasta adjudicada");
        }

        boolean esVendedor = subasta.getVendedor().getId().equals(usuario.getId());
        boolean esGanador = subasta.getGanador() != null
                && subasta.getGanador().getId().equals(usuario.getId());

        if (!esVendedor && !esGanador) {
            throw new IllegalArgumentException("Solo el vendedor o el ganador pueden abrir una disputa");
        }

        LocalDateTime ahora = LocalDateTime.now(Clock.systemUTC());

        Disputa disputa = Disputa.builder()
                .subasta(subasta)
                .iniciadaPor(usuario)
                .motivo(dto.motivo())
                .descripcion(dto.descripcion())
                .fechaCreacion(ahora)
                .build();

        subasta.setEstado(EstadoSubasta.EN_DISPUTA);
        subastaRepository.save(subasta);

        return disputaRepository.save(disputa);
    }

    @Transactional
    public Disputa resolverDisputa(Long disputaId, Long adminId, ResolverDisputaDTO dto) {
        Disputa disputa = disputaRepository.findById(disputaId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la disputa indicada"));

        Usuario admin = usuarioRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el administrador indicado"));

        if (admin.getRoles() == null || !admin.getRoles().contains(RolUsuario.ADMIN)) {
            throw new IllegalArgumentException("Solo un administrador puede resolver disputas");
        }

        EstadoSubasta estadoFinal = dto.estadoFinal();

        if (estadoFinal != EstadoSubasta.ADJUDICADA
                && estadoFinal != EstadoSubasta.FINALIZADA
                && estadoFinal != EstadoSubasta.CANCELADA) {
            throw new IllegalArgumentException("Estado final inválido para resolver disputa");
        }

        Subasta subasta = disputa.getSubasta();

        if (subasta.getEstado() != EstadoSubasta.EN_DISPUTA) {
            throw new IllegalArgumentException("La subasta no se encuentra en disputa");
        }

        LocalDateTime ahora = LocalDateTime.now(Clock.systemUTC());

        subasta.setEstado(estadoFinal);

        disputa.setResueltaPor(admin);
        disputa.setFechaResolucion(ahora);
        disputa.setResolucionAdmin(dto.resolucion());

        subastaRepository.save(subasta);

        return disputaRepository.save(disputa);
    }

    public List<Disputa> listar() {
        return disputaRepository.findAll();
    }
}