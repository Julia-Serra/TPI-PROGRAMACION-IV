package com.subastas.repository;

import com.subastas.entity.Puja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PujaRepository extends JpaRepository<Puja, Long> {

    List<Puja> findBySubastaIdOrderByMontoDesc(Long subastaId);

}