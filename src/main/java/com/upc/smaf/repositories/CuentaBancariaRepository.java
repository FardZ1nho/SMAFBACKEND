package com.upc.smaf.repositories;

import com.upc.smaf.entities.CuentaBancaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CuentaBancariaRepository extends JpaRepository<CuentaBancaria, Integer> {
    // Para mostrar en el combo solo las que usas actualmente
    List<CuentaBancaria> findByActivaTrue();
}