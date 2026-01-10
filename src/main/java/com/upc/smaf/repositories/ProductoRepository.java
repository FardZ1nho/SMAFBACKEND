package com.upc.smaf.repositories;

import com.upc.smaf.entities.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // ========== BÚSQUEDAS POR ATRIBUTOS ==========

    Optional<Producto> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    // ========== FILTROS POR ESTADO ==========

    List<Producto> findByActivoTrue();
    List<Producto> findByActivoFalse();
    Optional<Producto> findByCodigoAndActivoTrue(String codigo);
    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    @Query("SELECT p FROM Producto p WHERE p.stockActual < p.stockMinimo AND p.activo = true")
    List<Producto> findByStockActualLessThanStockMinimoAndActivoTrue();

    // ========== FILTROS POR STOCK ==========

    List<Producto> findByStockActualLessThanAndActivoTrue(Integer stockMinimo);
    List<Producto> findByStockActualEqualsAndActivoTrue(Integer stock);

    @Query("SELECT p FROM Producto p WHERE p.stockActual < p.stockMinimo AND p.activo = true")
    List<Producto> findProductosNecesitanReorden();

    List<Producto> findByStockActualBetweenAndActivoTrue(Integer min, Integer max);

    // ========== FILTROS POR PRECIO (ACTUALIZADOS) ==========

    List<Producto> findByPrecioChinaBetween(BigDecimal min, BigDecimal max);

    /**
     * Productos por rango de costo total
     */
    List<Producto> findByCostoTotalBetween(BigDecimal min, BigDecimal max);

    /**
     * Productos por rango de precio de venta
     */
    List<Producto> findByPrecioVentaBetween(BigDecimal min, BigDecimal max);

    /**
     * Productos con precio de venta menor o igual a cierto valor
     */
    List<Producto> findByPrecioVentaLessThanEqual(BigDecimal precioMax);

    // ========== OPERACIONES DE STOCK ==========

    @Modifying
    @Transactional
    @Query("UPDATE Producto p SET p.stockActual = p.stockActual + :cantidad WHERE p.id = :id")
    void aumentarStock(@Param("id") Integer id, @Param("cantidad") Integer cantidad);

    @Modifying
    @Transactional
    @Query("UPDATE Producto p SET p.stockActual = p.stockActual - :cantidad WHERE p.id = :id AND p.stockActual >= :cantidad")
    int disminuirStock(@Param("id") Integer id, @Param("cantidad") Integer cantidad);

    // ========== CONSULTAS DE AGREGACIÓN ==========

    @Query("SELECT SUM(p.stockActual) FROM Producto p WHERE p.activo = true")
    Long getStockTotal();

    @Query("SELECT SUM(p.stockActual * p.costoTotal) FROM Producto p WHERE p.activo = true AND p.costoTotal IS NOT NULL")
    BigDecimal getValorTotalInventario();

    @Query("SELECT " +
            "SUM(CASE WHEN p.stockActual = 0 THEN 1 ELSE 0 END) as agotados, " +
            "SUM(CASE WHEN p.stockActual > 0 AND p.stockActual < p.stockMinimo THEN 1 ELSE 0 END) as bajos, " +
            "SUM(CASE WHEN p.stockActual >= p.stockMinimo THEN 1 ELSE 0 END) as normales " +
            "FROM Producto p WHERE p.activo = true")
    Object[] getResumenStock();


    // ========== AGREGAR ESTOS MÉTODOS AL FINAL DE ProductoRepository ==========

    /**
     * Cuenta la cantidad de productos activos
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.activo = true")
    Integer contarProductosActivos();

    /**
     * Cuenta productos con stock bajo (stock actual < stock mínimo)
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.stockActual < p.stockMinimo AND p.activo = true")
    Integer contarProductosStockBajo();



    // ========== ORDENAMIENTOS ==========

    List<Producto> findByActivoTrueOrderByNombreAsc();
    List<Producto> findByActivoTrueOrderByStockActualAsc();
    List<Producto> findByActivoTrueOrderByFechaCreacionDesc();
}