package com.subastas.repository;

import com.subastas.entity.Subasta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubastaRepository extends JpaRepository<Subasta, Long> {
}