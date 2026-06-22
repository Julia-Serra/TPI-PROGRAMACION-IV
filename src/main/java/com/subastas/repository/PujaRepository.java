package com.subastas.repository;

import com.subastas.entity.Puja;
import com.subastas.entity.Subasta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PujaRepository extends JpaRepository<Puja, Long> {

    Optional<Puja> findTopBySubastaOrderByMontoDesc(Subasta subasta);
}