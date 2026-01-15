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

    // Buscar por serie y número (para evitar duplicados de facturas del mismo proveedor)
    Optional<Compra> findBySerieAndNumeroAndProveedorId(String serie, String numero, Integer proveedorId);

    // Listar compras de un proveedor específico
    List<Compra> findByProveedorId(Integer proveedorId);

    // Buscar compras por número de comprobante
    @Query("SELECT c FROM Compra c WHERE c.numero LIKE %:numero%")
    List<Compra> buscarPorNumero(@Param("numero") String numero);
}