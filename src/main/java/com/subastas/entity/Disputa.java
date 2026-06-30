package com.subastas.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "disputas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Disputa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Subasta subasta;

    @ManyToOne(optional = false)
    private Usuario iniciadaPor;

    @Column(nullable = false)
    private String motivo;

    @Column(length = 1500)
    private String descripcion;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaResolucion;

    @ManyToOne
    private Usuario resueltaPor;

    @Column(length = 1500)
    private String resolucionAdmin;
}