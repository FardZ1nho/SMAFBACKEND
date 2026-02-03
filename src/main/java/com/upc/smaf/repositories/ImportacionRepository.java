package com.upc.smaf.repositories;

import com.upc.smaf.entities.Importacion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImportacionRepository extends JpaRepository<Importacion, Integer> {

    // Buscar por el código de texto (Ej: "CHINA-2026-01")
    Optional<Importacion> findByCodigoAgrupador(String codigoAgrupador);

    // ✅ QUERY PARA EL DASHBOARD (Próximas llegadas)
    // Trae las importaciones con fecha definida, ordenadas por la más cercana
    @Query("SELECT i FROM Importacion i WHERE i.fechaEstimadaLlegada IS NOT NULL ORDER BY i.fechaEstimadaLlegada ASC")
    List<Importacion> findProximasLlegadas(Pageable pageable);

    // ❌ ELIMINADO: findByCompraId(Integer id)
    // (Este era el causante del error porque ya no existe la relación directa en la entidad)
}