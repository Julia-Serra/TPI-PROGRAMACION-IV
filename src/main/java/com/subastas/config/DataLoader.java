package com.subastas.config;

import com.subastas.entity.Categoria;
import com.subastas.entity.Producto;
import com.subastas.entity.Subasta;
import com.subastas.entity.Usuario;
import com.subastas.enums.EstadoSubasta;
import com.subastas.enums.RolUsuario;
import com.subastas.repository.CategoriaRepository;
import com.subastas.repository.ProductoRepository;
import com.subastas.repository.SubastaRepository;
import com.subastas.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final SubastaRepository subastaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository,
            SubastaRepository subastaRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.subastaRepository = subastaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        Usuario vendedor = cargarUsuarioVendedor();

        Categoria relojes = cargarCategoria("Relojes");
        Categoria coleccionables = cargarCategoria("Coleccionables");
        Categoria arte = cargarCategoria("Arte");

        Producto rolex = cargarProducto(
                "Rolex Daytona Oro",
                "Reloj exclusivo de colección",
                relojes,
                vendedor
        );

        Producto ferrari = cargarProducto(
                "Ferrari F40 Miniatura",
                "Modelo escala 1:18 edición limitada",
                coleccionables,
                vendedor
        );

        Producto obra = cargarProducto(
                "Obra estilo Picasso",
                "Pieza única de colección privada",
                arte,
                vendedor
        );

        cargarSubasta(rolex, vendedor, BigDecimal.valueOf(5000), BigDecimal.valueOf(500), 3);
        cargarSubasta(ferrari, vendedor, BigDecimal.valueOf(1200), BigDecimal.valueOf(100), 5);
        cargarSubasta(obra, vendedor, BigDecimal.valueOf(8000), BigDecimal.valueOf(800), 7);

        System.out.println("✔ DataLoader ejecutado correctamente");
    }

    private Usuario cargarUsuarioVendedor() {

        return usuarioRepository.findByEmail("vendedor@test.com")
                .orElseGet(() -> {
                    Usuario vendedor = Usuario.builder()
                            .nombre("Vendedor Demo")
                            .email("vendedor@test.com")
                            .password(passwordEncoder.encode("1234"))
                            .roles(Set.of(RolUsuario.COMPRADOR, RolUsuario.VENDEDOR, RolUsuario.ADMIN))
                            .build();
                    System.out.println("✔ Usuario vendedor/admin creado");

                    return usuarioRepository.save(vendedor);
                });
    }

    private Categoria cargarCategoria(String nombre) {

        return categoriaRepository.findByNombre(nombre)
                .orElseGet(() -> {
                    Categoria categoria = Categoria.builder()
                            .nombre(nombre)
                            .build();

                    return categoriaRepository.save(categoria);
                });
    }

    private Producto cargarProducto(
            String titulo,
            String descripcion,
            Categoria categoria,
            Usuario vendedor
    ) {
        return productoRepository.findByTitulo(titulo)
                .orElseGet(() -> {
                    Producto producto = Producto.builder()
                            .titulo(titulo)
                            .descripcion(descripcion)
                            .categoria(categoria)
                            .vendedor(vendedor)
                            .moderado(true)
                            .eliminado(false)
                            .build();

                    return productoRepository.save(producto);
                });
    }

    private void cargarSubasta(
            Producto producto,
            Usuario vendedor,
            BigDecimal precioBase,
            BigDecimal incrementoMinimo,
            int diasDuracion
    ) {
        if (subastaRepository.existsByProductoId(producto.getId())) {
            return;
        }

        LocalDateTime ahora = LocalDateTime.now(Clock.systemUTC());

        Subasta subasta = Subasta.builder()
                .producto(producto)
                .precioBase(precioBase)
                .precioActual(precioBase)
                .incrementoMinimo(incrementoMinimo)
                .fechaInicio(ahora.minusMinutes(1))
                .fechaCierre(ahora.plusDays(diasDuracion))
                .estado(EstadoSubasta.ACTIVA)
                .vendedor(vendedor)
                .build();

        subastaRepository.save(subasta);

        System.out.println("✔ Subasta creada: " + producto.getTitulo());
    }
}