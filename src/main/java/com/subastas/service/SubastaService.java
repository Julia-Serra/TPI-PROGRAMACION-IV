package com.subastas.service;

import com.subastas.dto.CrearSubastaDTO;
import com.subastas.entity.Producto;
import com.subastas.entity.Puja;
import com.subastas.entity.Subasta;
import com.subastas.entity.Usuario;
import com.subastas.enums.EstadoSubasta;
import com.subastas.enums.RolUsuario;
import com.subastas.repository.ProductoRepository;
import com.subastas.repository.PujaRepository;
import com.subastas.repository.SubastaRepository;
import com.subastas.repository.UsuarioRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SubastaService {

    private final SubastaRepository subastaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PujaRepository pujaRepository;
    private final NotificacionService notificacionService;

    public SubastaService(
            SubastaRepository subastaRepository,
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository,
            PujaRepository pujaRepository,
            NotificacionService notificacionService
    ) {
        this.subastaRepository = subastaRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.pujaRepository = pujaRepository;
        this.notificacionService = notificacionService;
    }

    @Transactional
    public Subasta crearSubasta(CrearSubastaDTO dto, Long vendedorId) {
        Usuario vendedor = usuarioRepository.findById(vendedorId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el vendedor indicado"));

        if (vendedor.isBloqueado()) {
            throw new IllegalArgumentException("El vendedor está bloqueado");
        }

        if (vendedor.getRoles() == null ||
                (!vendedor.getRoles().contains(RolUsuario.VENDEDOR)
                        && !vendedor.getRoles().contains(RolUsuario.ADMIN))) {
            throw new IllegalArgumentException("El usuario no tiene permiso para crear subastas");
        }

        Producto producto = productoRepository.findById(dto.productoId())
                .orElseThrow(() -> new IllegalArgumentException("No existe el producto indicado"));

        if (!dto.fechaCierre().isAfter(dto.fechaInicio())) {
            throw new IllegalArgumentException("La fecha de cierre debe ser posterior a la fecha de inicio");
        }

        Subasta subasta = Subasta.builder()
                .producto(producto)
                .precioBase(dto.precioBase())
                .precioActual(dto.precioBase())
                .incrementoMinimo(dto.incrementoMinimo())
                .fechaInicio(dto.fechaInicio())
                .fechaCierre(dto.fechaCierre())
                .estado(EstadoSubasta.BORRADOR)
                .vendedor(vendedor)
                .build();

        return subastaRepository.save(subasta);
    }

    @Transactional
    public Subasta publicarSubasta(Long id, Long vendedorId) {
        Subasta subasta = subastaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe la subasta indicada"));

        Usuario usuario = usuarioRepository.findById(vendedorId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el usuario indicado"));

        boolean esVendedor = subasta.getVendedor().getId().equals(usuario.getId());
        boolean esAdmin = usuario.getRoles() != null && usuario.getRoles().contains(RolUsuario.ADMIN);

        if (!esVendedor && !esAdmin) {
            throw new IllegalArgumentException("No tenés permiso para publicar esta subasta");
        }

        if (subasta.getEstado() != EstadoSubasta.BORRADOR) {
            throw new IllegalArgumentException("Solo se puede publicar una subasta en borrador");
        }

        subasta.setEstado(EstadoSubasta.PUBLICADA);

        return subastaRepository.save(subasta);
    }

    @Transactional
    public List<Subasta> obtenerSubastasVisibles() {
        actualizarEstadosAutomaticamente();

        List<Subasta> visibles = new ArrayList<>();
        visibles.addAll(subastaRepository.findByEstado(EstadoSubasta.PUBLICADA));
        visibles.addAll(subastaRepository.findByEstado(EstadoSubasta.ACTIVA));
        visibles.addAll(subastaRepository.findByEstado(EstadoSubasta.FINALIZADA));
        visibles.addAll(subastaRepository.findByEstado(EstadoSubasta.ADJUDICADA));

        return visibles;
    }

    @Transactional
    public Optional<Subasta> obtenerDetalle(Long id) {
        actualizarEstadosAutomaticamente();
        return subastaRepository.findById(id);
    }

    @Transactional
    public void actualizarEstadosAutomaticamente() {
        activarSubastasPublicadas();
        cerrarSubastasVencidas();
    }

    @Transactional
    public int activarSubastasPublicadas() {
        LocalDateTime ahora = LocalDateTime.now(Clock.systemUTC());

        List<Subasta> publicadas = subastaRepository.findByFechaInicioBeforeAndEstado(
                ahora,
                EstadoSubasta.PUBLICADA
        );

        publicadas.forEach(subasta -> subasta.setEstado(EstadoSubasta.ACTIVA));
        subastaRepository.saveAll(publicadas);

        return publicadas.size();
    }

    @Transactional
    public int cerrarSubastasVencidas() {
        LocalDateTime ahora = LocalDateTime.now(Clock.systemUTC());

        List<Subasta> vencidas = subastaRepository.findByFechaCierreBeforeAndEstado(
                ahora,
                EstadoSubasta.ACTIVA
        );

        for (Subasta subasta : vencidas) {
            boolean tienePujas = pujaRepository.existsBySubastaId(subasta.getId());

            if (tienePujas) {

                Puja ultimaPuja = pujaRepository
                        .findTopBySubastaIdOrderByMontoDesc(subasta.getId())
                        .orElseThrow();

                subasta.setGanador(ultimaPuja.getComprador());
                subasta.setEstado(EstadoSubasta.ADJUDICADA);
                subasta.setPrecioFinal(subasta.getPrecioActual());
                subasta.setFechaAdjudicacion(ahora);

                notificacionService.crearNotificacion(
                        ultimaPuja.getComprador(),
                        "¡Ganaste la subasta!",
                        "Ganaste la subasta del producto: "
                                + subasta.getProducto().getTitulo()
                );

                notificacionService.crearNotificacion(
                        subasta.getVendedor(),
                        "Subasta finalizada",
                        "Tu subasta fue adjudicada correctamente."
                );

            } else {

                subasta.setEstado(EstadoSubasta.FINALIZADA);

                notificacionService.crearNotificacion(
                        subasta.getVendedor(),
                        "Subasta finalizada",
                        "La subasta finalizó sin recibir ofertas."
                );
            }
        }

        subastaRepository.saveAll(vencidas);

        return vencidas.size();
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void actualizarSubastasAutomaticamente() {
        actualizarEstadosAutomaticamente();
    }
}