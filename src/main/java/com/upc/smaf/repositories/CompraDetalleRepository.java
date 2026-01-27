package com.upc.smaf.repositories;

import com.upc.smaf.entities.CompraDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompraDetalleRepository extends JpaRepository<CompraDetalle, Integer> {

    // Obtener todos los productos de una compra específica
    List<CompraDetalle> findByCompraId(Integer compraId);

    // Buscar ingresos a un almacén específico a través de compras
    List<CompraDetalle> findByAlmacenId(Integer almacenId);

    // ✅ QUERY CORREGIDA:
    // En lugar de navegar "d.compra.importacion" (que no existe),
    // hacemos un cruce explícito entre Importacion (i) y CompraDetalle (d)
    // donde coincidan en la misma Compra.
    @Query("SELECT SUM(d.cantidad) FROM Importacion i, CompraDetalle d " +
            "WHERE i.compra.id = d.compra.id " +  // Aquí hacemos la unión manual
            "AND d.producto.id = :productoId " +
            "AND i.estado IN ('ORDENADO', 'EN_TRANSITO', 'EN_ADUANAS', 'NACIONALIZADO')")
    Integer obtenerStockPorLlegar(@Param("productoId") Integer productoId);
}