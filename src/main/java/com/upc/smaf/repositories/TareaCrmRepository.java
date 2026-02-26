package com.upc.smaf.repositories;

import com.upc.smaf.entities.EstadoTareaCrm;
import com.upc.smaf.entities.TareaCrm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TareaCrmRepository extends JpaRepository<TareaCrm, Long> {

    // Obtener todas las tareas de una cotización específica
    List<TareaCrm> findByCotizacionId(Integer cotizacionId);
    // Obtener todas las tareas según su estado (ej: para el Dashboard de Angular)
    List<TareaCrm> findByEstadoOrderByFechaLimiteAsc(EstadoTareaCrm estado);
}