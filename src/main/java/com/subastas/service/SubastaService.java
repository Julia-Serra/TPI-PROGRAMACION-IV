package com.subastas.service;

import com.subastas.dto.CrearSubastaDTO;
import com.subastas.entity.Subasta;
import com.subastas.entity.Usuario;
import com.subastas.enums.EstadoSubasta;
import com.subastas.repository.SubastaRepository;
import com.subastas.repository.UsuarioRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubastaService {

    private final SubastaRepository subastaRepository;
    private final UsuarioRepository usuarioRepository;

    public SubastaService(SubastaRepository subastaRepository, UsuarioRepository usuarioRepository) {
        this.subastaRepository = subastaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Subasta crearSubasta(Subasta subasta) {

        subasta.setEstado(EstadoSubasta.ACTIVA);
        subasta.setFechaInicio(LocalDateTime.now());
        subasta.setPrecioActual(subasta.getPrecioInicial());

        return subastaRepository.save(subasta);
    }

    @Transactional
    public Subasta crearSubasta(CrearSubastaDTO dto, Long vendedorId) {
        Usuario vendedor = usuarioRepository.findById(vendedorId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el vendedor indicado"));

        Subasta subasta = Subasta.builder()
                .titulo(dto.titulo())
                .descripcion(dto.descripcion())
                .precioInicial(dto.precioInicial())
                .fechaFin(dto.fechaFin())
                .vendedor(vendedor)
                .build();

        return crearSubasta(subasta);
    }

    @Transactional
    public List<Subasta> obtenerSubastasActivas() {
        cerrarSubastasVencidas();
        return subastaRepository.findByEstado(EstadoSubasta.ACTIVA);
    }

    @Transactional
    public Optional<Subasta> obtenerDetalle(Long id) {
        Optional<Subasta> subasta = subastaRepository.findById(id);
        subasta.ifPresent(this::finalizarSiEstaVencida);
        return subasta;
    }

    @Transactional
    public int cerrarSubastasVencidas() {
        List<Subasta> vencidas = subastaRepository.findByFechaFinBeforeAndEstado(
                LocalDateTime.now(),
                EstadoSubasta.ACTIVA
        );

        vencidas.forEach(subasta -> subasta.setEstado(EstadoSubasta.FINALIZADA));
        subastaRepository.saveAll(vencidas);

        return vencidas.size();
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cerrarSubastasVencidasAutomaticamente() {
        cerrarSubastasVencidas();
    }

    private void finalizarSiEstaVencida(Subasta subasta) {
        if (subasta.getEstado() == EstadoSubasta.ACTIVA && !subasta.getFechaFin().isAfter(LocalDateTime.now())) {
            subasta.setEstado(EstadoSubasta.FINALIZADA);
            subastaRepository.save(subasta);
        }
    }

}
