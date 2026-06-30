package com.subastas.repository;

import com.subastas.entity.Puja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PujaRepository extends JpaRepository<Puja, Long> {

    Optional<Puja> findTopBySubastaIdOrderByMontoDesc(Long subastaId);

    List<Puja> findBySubastaIdOrderByMontoDesc(Long subastaId);

    List<Puja> findBySubastaIdOrderByFechaHoraDesc(Long subastaId);

    List<Puja> findByCompradorIdOrderByFechaHoraDesc(Long compradorId);

    boolean existsBySubastaId(Long subastaId);
}