package com.upc.smaf.repositories;

import com.upc.smaf.entities.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Integer> {

    // Buscar duplicados
    Optional<Compra> findBySerieAndNumeroAndProveedorId(String serie, String numero, Integer proveedorId);

    // Listar por proveedor
    List<Compra> findByProveedorId(Integer proveedorId);

    // Búsqueda general
    @Query("SELECT c FROM Compra c WHERE c.numero LIKE %:numero%")
    List<Compra> buscarPorNumero(@Param("numero") String numero);

    // ✅ NUEVO: Buscar facturas por el CÓDIGO DE TEXTO (Ej: dame todas las de "2026-01")
    // Útil para ver qué facturas puso el usuario con ese código antes de crear la carpeta
    List<Compra> findByCodImportacion(String codImportacion);

    // ✅ NUEVO: Buscar facturas por la RELACIÓN REAL (Foreign Key)
    List<Compra> findByImportacionId(Integer importacionId);
}