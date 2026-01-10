package com.upc.smaf.repositories;

import com.upc.smaf.dtos.response.ProductoVendidoDTO;
import com.upc.smaf.entities.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Integer> {
    /**
     * Obtiene los productos más vendidos
     */
    @Query("SELECT new com.upc.smaf.dtos.response.ProductoVendidoDTO(" +
            "p.id, " +
            "p.nombre, " +
            "p.codigo, " +
            "SUM(dv.cantidad), " +
            "SUM(dv.subtotal)) " +
            "FROM DetalleVenta dv " +
            "JOIN dv.producto p " +
            "WHERE p.activo = true " +
            "GROUP BY p.id, p.nombre, p.codigo " +
            "ORDER BY SUM(dv.cantidad) DESC")
    List<ProductoVendidoDTO> findProductosMasVendidos(@Param("limit") int limit);
}