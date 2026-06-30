package com.subastas.entity;

import com.subastas.enums.EstadoSubasta;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subastas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subasta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false)
    private BigDecimal precioInicial;

    @Column(nullable = false)
    private BigDecimal precioActual;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(nullable = false)
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSubasta estado;

    @ManyToOne
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Usuario vendedor;

    @Version
    private Long version;

    ManyToOne(optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal precioBase;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal precioActual;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal incrementoMinimo;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaCierre;

    @ManyToOne
    @JoinColumn(name = "ganador_id")
    private Usuario ganador;

    @Column(precision = 19, scale = 2)
    private BigDecimal precioFinal;

    private LocalDateTime fechaAdjudicacion;
}