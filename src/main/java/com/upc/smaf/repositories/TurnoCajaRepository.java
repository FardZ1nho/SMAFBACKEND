package com.upc.smaf.repositories;

import com.upc.smaf.entities.TurnoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TurnoCajaRepository extends JpaRepository<TurnoCaja, Integer> {
    // Busca si hay alguna caja abierta actualmente
    Optional<TurnoCaja> findByEstado(String estado);
}