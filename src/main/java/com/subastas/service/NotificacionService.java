package com.subastas.service;

import com.subastas.entity.Notificacion;
import com.subastas.entity.Usuario;
import com.subastas.repository.NotificacionRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    public Notificacion crearNotificacion(Usuario usuario, String titulo, String mensaje) {
        Notificacion notificacion = Notificacion.builder()
                .usuario(usuario)
                .titulo(titulo)
                .mensaje(mensaje)
                .fecha(LocalDateTime.now(Clock.systemUTC()))
                .leida(false)
                .build();

        return notificacionRepository.save(notificacion);
    }

    public List<Notificacion> listarPorUsuario(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    public Notificacion marcarComoLeida(Long id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe la notificación indicada"));

        notificacion.setLeida(true);

        return notificacionRepository.save(notificacion);
    }
}