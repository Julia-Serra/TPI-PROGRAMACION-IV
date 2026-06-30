package com.subastas.config;

import com.subastas.entity.Subasta;
import com.subastas.entity.Usuario;
import com.subastas.enums.EstadoSubasta;
import com.subastas.enums.RolUsuario;
import com.subastas.repository.SubastaRepository;
import com.subastas.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final SubastaRepository subastaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UsuarioRepository usuarioRepository,
                      SubastaRepository subastaRepository,
                      PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.subastaRepository = subastaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        Usuario vendedor = cargarUsuarios();
        cargarSubastas(vendedor);

        System.out.println("✔ DataLoader ejecutado correctamente");
    }

    private Usuario cargarUsuarios() {

        if (usuarioRepository.count() > 0) {
            return usuarioRepository.findAll().get(0);
        }

        Usuario admin = Usuario.builder()
                .nombre("Admin")
                .email("admin@test.com")
                .password(passwordEncoder.encode("1234"))
                .rol(RolUsuario.VENDEDOR)
                .build();

        Usuario saved = usuarioRepository.save(admin);

        System.out.println("✔ Usuario creado");

        return saved;
    }

    private void cargarSubastas(Usuario vendedor) {

        if (subastaRepository.count() > 0) return;

        LocalDateTime now = LocalDateTime.now();

        Subasta s1 = Subasta.builder()
                .titulo("Rolex Daytona Oro")
                .descripcion("Reloj exclusivo de colección")
                .precioInicial(BigDecimal.valueOf(5000))
                .precioActual(BigDecimal.valueOf(5000))
                .fechaInicio(now)
                .fechaFin(now.plusDays(3))
                .estado(EstadoSubasta.ACTIVA)
                .vendedor(vendedor)
                .build();

        Subasta s2 = Subasta.builder()
                .titulo("Ferrari F40 Miniatura")
                .descripcion("Modelo escala 1:18 edición limitada")
                .precioInicial(BigDecimal.valueOf(1200))
                .precioActual(BigDecimal.valueOf(1200))
                .fechaInicio(now)
                .fechaFin(now.plusDays(5))
                .estado(EstadoSubasta.ACTIVA)
                .vendedor(vendedor)
                .build();

        Subasta s3 = Subasta.builder()
                .titulo("Obra estilo Picasso")
                .descripcion("Pieza única de colección privada")
                .precioInicial(BigDecimal.valueOf(8000))
                .precioActual(BigDecimal.valueOf(8000))
                .fechaInicio(now)
                .fechaFin(now.plusDays(7))
                .estado(EstadoSubasta.ACTIVA)
                .vendedor(vendedor)
                .build();

        subastaRepository.saveAll(List.of(s1, s2, s3));

        System.out.println("✔ Subastas creadas");
    }
}