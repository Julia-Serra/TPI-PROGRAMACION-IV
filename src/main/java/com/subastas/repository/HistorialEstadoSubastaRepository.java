package com.subastas.repository;

import com.subastas.entity.HistorialEstadoSubasta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialEstadoSubastaRepository extends JpaRepository<HistorialEstadoSubasta, Long> {

    List<HistorialEstadoSubasta> findBySubastaIdOrderByFechaDesc(Long subastaId);
}