package com.upc.smaf.repositories;

import com.upc.smaf.dtos.response.TareaCrmResponseDTO;
import com.upc.smaf.entities.Cotizacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CotizacionRepository extends JpaRepository<Cotizacion,Integer> {
    // En el Service y ServiceImpl
}
