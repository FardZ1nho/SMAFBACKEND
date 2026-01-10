package com.upc.smaf.repositories;

import com.upc.smaf.entities.Movimiento;
import com.upc.smaf.entities.Movimiento.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    // ========== BÚSQUEDAS BÁSICAS ==========

    /**
     * Buscar movimiento por código único
     */
    Optional<Movimiento> findByCodigo(String codigo);

    /**
     * Verificar si existe un código
     */
    boolean existsByCodigo(String codigo);

    // ========== FILTROS POR PRODUCTO ==========

    /**
     * Listar todos los movimientos de un producto
     */
    List<Movimiento> findByProductoIdOrderByFechaMovimientoDesc(Integer productoId);

    /**
     * Listar movimientos de un producto por tipo
     */
    List<Movimiento> findByProductoIdAndTipoMovimientoOrderByFechaMovimientoDesc(
            Integer productoId, TipoMovimiento tipoMovimiento);

    // ========== FILTROS POR ALMACÉN ==========

    /**
     * Listar movimientos donde el almacén es origen
     */
    List<Movimiento> findByAlmacenOrigenIdOrderByFechaMovimientoDesc(Long almacenId);

    /**
     * Listar movimientos donde el almacén es destino
     */
    List<Movimiento> findByAlmacenDestinoIdOrderByFechaMovimientoDesc(Long almacenId);

    /**
     * Listar todos los movimientos relacionados con un almacén (origen o destino)
     */
    @Query("SELECT m FROM Movimiento m WHERE m.almacenOrigen.id = :almacenId OR m.almacenDestino.id = :almacenId " +
            "ORDER BY m.fechaMovimiento DESC")
    List<Movimiento> findMovimientosPorAlmacen(@Param("almacenId") Long almacenId);

    // ========== FILTROS POR TIPO ==========

    /**
     * Listar movimientos por tipo
     */
    List<Movimiento> findByTipoMovimientoOrderByFechaMovimientoDesc(TipoMovimiento tipoMovimiento);

    /**
     * Listar solo traslados
     */
    @Query("SELECT m FROM Movimiento m WHERE m.tipoMovimiento = 'TRASLADO' " +
            "ORDER BY m.fechaMovimiento DESC")
    List<Movimiento> findTraslados();

    // ========== FILTROS POR FECHA ==========

    /**
     * Listar movimientos entre fechas
     */
    List<Movimiento> findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(
            LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Listar movimientos de un producto entre fechas
     */
    List<Movimiento> findByProductoIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(
            Integer productoId, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // ========== FILTROS COMBINADOS ==========

    /**
     * Listar traslados de un producto entre dos almacenes
     */
    @Query("SELECT m FROM Movimiento m WHERE m.producto.id = :productoId " +
            "AND m.almacenOrigen.id = :almacenOrigenId " +
            "AND m.almacenDestino.id = :almacenDestinoId " +
            "AND m.tipoMovimiento = 'TRASLADO' " +
            "ORDER BY m.fechaMovimiento DESC")
    List<Movimiento> findTrasladosEntreAlmacenes(
            @Param("productoId") Integer productoId,
            @Param("almacenOrigenId") Long almacenOrigenId,
            @Param("almacenDestinoId") Long almacenDestinoId);

    // ========== ESTADÍSTICAS ==========

    /**
     * Contar movimientos por tipo
     */
    @Query("SELECT COUNT(m) FROM Movimiento m WHERE m.tipoMovimiento = :tipo")
    Long contarPorTipo(@Param("tipo") TipoMovimiento tipo);

    /**
     * Calcular cantidad total movida de un producto
     */
    @Query("SELECT COALESCE(SUM(m.cantidad), 0) FROM Movimiento m " +
            "WHERE m.producto.id = :productoId AND m.tipoMovimiento = :tipo")
    Integer calcularCantidadMovidaPorTipo(
            @Param("productoId") Integer productoId,
            @Param("tipo") TipoMovimiento tipo);

    /**
     * Obtener últimos N movimientos
     */
    List<Movimiento> findTop10ByOrderByFechaMovimientoDesc();

    // ========== GENERACIÓN DE CÓDIGO ==========

    /**
     * Obtener el último código generado para generar el siguiente
     */
    @Query("SELECT m.codigo FROM Movimiento m ORDER BY m.id DESC LIMIT 1")
    Optional<String> findUltimoCodigo();
}