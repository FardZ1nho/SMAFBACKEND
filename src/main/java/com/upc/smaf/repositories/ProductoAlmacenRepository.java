package com.upc.smaf.repositories;

import com.upc.smaf.entities.Almacen;
import com.upc.smaf.entities.Producto;
import com.upc.smaf.entities.ProductoAlmacen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoAlmacenRepository extends JpaRepository<ProductoAlmacen, Long> {

    // Buscar todas las ubicaciones de un producto
    List<ProductoAlmacen> findByProductoId(Integer productoId);
    Optional<ProductoAlmacen> findByProductoAndAlmacen(Producto producto, Almacen almacen);

    // Buscar todos los productos en un almacén
    List<ProductoAlmacen> findByAlmacenId(Long almacenId);

    // Buscar ubicación específica de un producto en un almacén
    Optional<ProductoAlmacen> findByProductoIdAndAlmacenId(Integer productoId, Long almacenId);

    // Listar solo registros activos de un producto
    List<ProductoAlmacen> findByProductoIdAndActivoTrue(Integer productoId);

    // Listar solo registros activos de un almacén
    List<ProductoAlmacen> findByAlmacenIdAndActivoTrue(Long almacenId);

    // Verificar si existe un producto en un almacén
    boolean existsByProductoIdAndAlmacenId(Integer productoId, Long almacenId);

    // Calcular stock total de un producto (suma de todos los almacenes)
    @Query("SELECT COALESCE(SUM(pa.stock), 0) FROM ProductoAlmacen pa " +
            "WHERE pa.producto.id = :productoId AND pa.activo = true")
    Integer calcularStockTotalProducto(@Param("productoId") Integer productoId);
    // ✅ ESTE ES EL MÉTODO QUE TE FALTABA:
    // Devuelve todas las filas de ese producto en todos los almacenes para poder sumar el stock
    List<ProductoAlmacen> findByProducto(Producto producto);
    // Productos con stock bajo en un almacén específico
    @Query("SELECT pa FROM ProductoAlmacen pa " +
            "WHERE pa.almacen.id = :almacenId AND pa.activo = true " +
            "AND pa.stockMinimo IS NOT NULL AND pa.stock <= pa.stockMinimo")
    List<ProductoAlmacen> findProductosConStockBajoEnAlmacen(@Param("almacenId") Long almacenId);
}