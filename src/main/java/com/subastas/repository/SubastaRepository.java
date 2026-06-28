package com.subastas.repository;

import com.subastas.entity.Subasta;
import com.subastas.enums.EstadoSubasta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SubastaRepository extends JpaRepository<Subasta, Long> {

    List<Subasta> findByEstado(EstadoSubasta estado);

    List<Subasta> findByFechaFinBeforeAndEstado(LocalDateTime fecha, EstadoSubasta estado);

}