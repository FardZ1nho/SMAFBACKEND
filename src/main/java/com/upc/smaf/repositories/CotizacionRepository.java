package com.upc.smaf.repositories;

import com.upc.smaf.entities.Cotizacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CotizacionRepository extends JpaRepository<Cotizacion,Integer> {
}
