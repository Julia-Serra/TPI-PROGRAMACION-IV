package com.subastas.repository;

import com.subastas.entity.Subasta;
import com.subastas.enums.EstadoSubasta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubastaRepository extends JpaRepository<Subasta, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Subasta s WHERE s.id = :id")
    Optional<Subasta> findByIdForUpdate(@Param("id") Long id);

    List<Subasta> findByEstado(EstadoSubasta estado);

    List<Subasta> findByFechaInicioBeforeAndEstado(
            LocalDateTime fecha,
            EstadoSubasta estado
    );

    List<Subasta> findByFechaCierreBeforeAndEstado(
            LocalDateTime fecha,
            EstadoSubasta estado
    );
}