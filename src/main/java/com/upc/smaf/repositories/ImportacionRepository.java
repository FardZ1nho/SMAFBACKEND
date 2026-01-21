package com.upc.smaf.repositories;

import com.upc.smaf.entities.EstadoImportacion;
import com.upc.smaf.entities.Importacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImportacionRepository extends JpaRepository<Importacion, Integer> {
    // Buscar la importación asociada a una compra específica
    Optional<Importacion> findByCompraId(Integer compraId);

    // Listar importaciones por estado (Ej: Para ver solo las que están EN_TRANSITO)
    List<Importacion> findByEstado(EstadoImportacion estado);
}