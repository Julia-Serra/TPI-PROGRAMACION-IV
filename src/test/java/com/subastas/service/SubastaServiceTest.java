package com.subastas.service;

import com.subastas.entity.Subasta;
import com.subastas.entity.Usuario;
import com.subastas.enums.EstadoSubasta;
import com.subastas.repository.SubastaRepository;
import com.subastas.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubastaServiceTest {

    private final SubastaRepository subastaRepository = mock(SubastaRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final SubastaService subastaService = new SubastaService(subastaRepository, usuarioRepository);

    @Test
    void crearSubastaInicializaEstadoFechaInicioYPrecioActual() {
        Subasta subasta = Subasta.builder()
                .titulo("Notebook")
                .descripcion("Notebook usada")
                .precioInicial(new BigDecimal("100.00"))
                .fechaFin(LocalDateTime.now().plusDays(1))
                .vendedor(Usuario.builder().id(1L).build())
                .build();

        when(subastaRepository.save(subasta)).thenReturn(subasta);

        Subasta creada = subastaService.crearSubasta(subasta);

        assertEquals(EstadoSubasta.ACTIVA, creada.getEstado());
        assertEquals(new BigDecimal("100.00"), creada.getPrecioActual());
        verify(subastaRepository).save(subasta);
    }

    @Test
    void cerrarSubastasVencidasFinalizaLasActivasConFechaPasada() {
        Subasta vencida = Subasta.builder()
                .estado(EstadoSubasta.ACTIVA)
                .fechaFin(LocalDateTime.now().minusMinutes(1))
                .build();

        when(subastaRepository.findByFechaFinBeforeAndEstado(
                org.mockito.ArgumentMatchers.any(LocalDateTime.class),
                org.mockito.ArgumentMatchers.eq(EstadoSubasta.ACTIVA)
        )).thenReturn(List.of(vencida));

        int cerradas = subastaService.cerrarSubastasVencidas();

        assertEquals(1, cerradas);
        assertEquals(EstadoSubasta.FINALIZADA, vencida.getEstado());
        verify(subastaRepository).saveAll(anyList());
    }
}
