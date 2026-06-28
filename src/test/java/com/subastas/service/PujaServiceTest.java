package com.subastas.service;

import com.subastas.dto.PujaDTO;
import com.subastas.entity.Puja;
import com.subastas.entity.Subasta;
import com.subastas.entity.Usuario;
import com.subastas.enums.EstadoSubasta;
import com.subastas.repository.PujaRepository;
import com.subastas.repository.SubastaRepository;
import com.subastas.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PujaServiceTest {

    private final PujaRepository pujaRepository = mock(PujaRepository.class);
    private final SubastaRepository subastaRepository = mock(SubastaRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final PujaService pujaService = new PujaService(pujaRepository, subastaRepository, usuarioRepository);

    @Test
    void realizarPujaActualizaPrecioActual() {
        Subasta subasta = subastaActiva(LocalDateTime.now().plusHours(1));
        Usuario comprador = Usuario.builder().id(2L).build();

        when(subastaRepository.findById(1L)).thenReturn(Optional.of(subasta));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(comprador));
        when(pujaRepository.save(any(Puja.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Puja puja = pujaService.realizarPuja(1L, 2L, new PujaDTO(new BigDecimal("150.00")));

        assertEquals(new BigDecimal("150.00"), puja.getMonto());
        assertEquals(new BigDecimal("150.00"), subasta.getPrecioActual());
        verify(subastaRepository).save(subasta);
    }

    @Test
    void realizarPujaRechazaMontoMenorOIgualAlPrecioActual() {
        Subasta subasta = subastaActiva(LocalDateTime.now().plusHours(1));
        Usuario comprador = Usuario.builder().id(2L).build();

        when(subastaRepository.findById(1L)).thenReturn(Optional.of(subasta));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(comprador));

        assertThrows(IllegalArgumentException.class,
                () -> pujaService.realizarPuja(1L, 2L, new PujaDTO(new BigDecimal("100.00"))));
    }

    @Test
    void realizarPujaEnUltimosCincoMinutosExtiendeLaSubasta() {
        LocalDateTime fechaFinOriginal = LocalDateTime.now().plusMinutes(4);
        Subasta subasta = subastaActiva(fechaFinOriginal);
        Usuario comprador = Usuario.builder().id(2L).build();

        when(subastaRepository.findById(1L)).thenReturn(Optional.of(subasta));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(comprador));
        when(pujaRepository.save(any(Puja.class))).thenAnswer(invocation -> invocation.getArgument(0));

        pujaService.realizarPuja(1L, 2L, new PujaDTO(new BigDecimal("150.00")));

        assertEquals(fechaFinOriginal.plusMinutes(5), subasta.getFechaFin());
    }

    private Subasta subastaActiva(LocalDateTime fechaFin) {
        return Subasta.builder()
                .id(1L)
                .titulo("Notebook")
                .descripcion("Notebook usada")
                .precioInicial(new BigDecimal("100.00"))
                .precioActual(new BigDecimal("100.00"))
                .fechaInicio(LocalDateTime.now().minusHours(1))
                .fechaFin(fechaFin)
                .estado(EstadoSubasta.ACTIVA)
                .vendedor(Usuario.builder().id(1L).build())
                .build();
    }
}
