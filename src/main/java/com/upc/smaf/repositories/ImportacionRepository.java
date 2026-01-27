package com.upc.smaf.repositories;

import com.upc.smaf.entities.EstadoImportacion;
import com.upc.smaf.entities.Importacion;
// ✅ IMPORTANTE: Este es el Pageable correcto para Spring Data (Base de Datos)
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImportacionRepository extends JpaRepository<Importacion, Integer> {

    // Buscar la importación asociada a una compra específica
    Optional<Importacion> findByCompraId(Integer compraId);

    // Listar importaciones por estado (Ej: Para ver solo las que están EN_TRANSITO)
    List<Importacion> findByEstado(EstadoImportacion estado);

    // ✅ Consulta para el Dashboard (Próximas llegadas)
    // Busca importaciones activas con fecha futura, ordenadas por la más próxima
    @Query("SELECT i FROM Importacion i " +
            "WHERE i.estado IN ('EN_TRANSITO', 'EN_ADUANAS') " +
            "AND i.fechaEstimadaLlegada IS NOT NULL " +
            "ORDER BY i.fechaEstimadaLlegada ASC")
    List<Importacion> findProximasLlegadas(Pageable pageable);
}