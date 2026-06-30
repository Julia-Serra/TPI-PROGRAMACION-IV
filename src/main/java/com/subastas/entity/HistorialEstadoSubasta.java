package com.subastas.entity;

import com.subastas.enums.EstadoSubasta;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_estados_subasta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstadoSubasta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Subasta subasta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSubasta estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSubasta estadoNuevo;

    @ManyToOne(optional = false)
    private Usuario usuarioResponsable;

    @Column(length = 1000)
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fecha;
}