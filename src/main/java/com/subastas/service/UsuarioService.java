package com.subastas.service;

import com.subastas.entity.Usuario;
import com.subastas.enums.RolUsuario;
import com.subastas.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Usuario bloquear(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el usuario indicado"));

        usuario.setBloqueado(true);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario desbloquear(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el usuario indicado"));

        usuario.setBloqueado(false);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizarRoles(Long id, Set<RolUsuario> roles) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el usuario indicado"));

        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("El usuario debe tener al menos un rol");
        }

        usuario.setRoles(roles);

        return usuarioRepository.save(usuario);
    }
}