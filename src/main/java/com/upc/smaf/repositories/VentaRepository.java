package com.upc.smaf.repositories;

import com.upc.smaf.dtos.ReporteMetodoPagoDTO;
import com.upc.smaf.entities.Venta;
import com.upc.smaf.entities.EstadoVenta;
import org.springframework.data.domain.Pageable; // ✅ IMPORTANTE
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    Optional<Venta> findByCodigo(String codigo);
    List<Venta> findByEstado(EstadoVenta estado);
    List<Venta> findByFechaVentaBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT v FROM Venta v WHERE v.nombreCliente LIKE %:nombre%")
    List<Venta> buscarPorCliente(@Param("nombre") String nombre);

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.estado = :estado")
    Long contarPorEstado(@Param("estado") EstadoVenta estado);

    Optional<Venta> findByNumeroDocumento(String numeroDocumento);
    boolean existsByNumeroDocumento(String numeroDocumento);
    List<Venta> findByTipoDocumento(String tipoDocumento);

    // ========== ESTADÍSTICAS SIMPLES ==========
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin")
    BigDecimal sumarVentasEntreFechas(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin")
    Integer contarVentasEntreFechas(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin AND v.estado = 'COMPLETADA'")
    BigDecimal sumarVentasCompletadasEntreFechas(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin AND v.estado = 'COMPLETADA'")
    Integer contarVentasCompletadasEntreFechas(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // ==========================================
    // 📈 CONSULTAS PARA GRÁFICOS (RAW DATE)
    // ==========================================
    @Query(value = "SELECT DATE(v.fecha_venta) as fecha, " +
            "COALESCE(SUM(v.total), 0) as total, " +
            "COUNT(v.id) as cantidad " +
            "FROM ventas v " +
            "WHERE v.fecha_venta >= :inicio AND v.fecha_venta <= :fin " +
            "AND v.estado = 'COMPLETADA' " +
            "GROUP BY DATE(v.fecha_venta) " +
            "ORDER BY DATE(v.fecha_venta) ASC", nativeQuery = true)
    List<Object[]> obtenerVentasPorDiaRaw(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query(value = "SELECT TO_CHAR(v.fecha_venta, 'MM') as mes, " +
            "COALESCE(SUM(v.total), 0) as total, " +
            "COUNT(v.id) as cantidad " +
            "FROM ventas v " +
            "WHERE v.fecha_venta >= :inicio AND v.fecha_venta <= :fin " +
            "AND v.estado = 'COMPLETADA' " +
            "GROUP BY TO_CHAR(v.fecha_venta, 'MM') " +
            "ORDER BY mes ASC", nativeQuery = true)
    List<Object[]> obtenerVentasPorMesRaw(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // ==========================================
    // 📊 REPORTE MÉTODOS DE PAGO
    // ==========================================
    @Query("SELECT new com.upc.smaf.dtos.ReporteMetodoPagoDTO(" +
            "p.metodoPago, SUM(p.monto), COUNT(p)) " +
            "FROM Venta v " +
            "JOIN v.pagos p " +
            "WHERE v.fechaVenta BETWEEN :inicio AND :fin " +
            "AND v.estado <> 'ANULADA' " +
            "AND v.estado <> 'CANCELADA' " +
            "GROUP BY p.metodoPago " +
            "ORDER BY SUM(p.monto) DESC")
    List<ReporteMetodoPagoDTO> obtenerReporteMetodosPago(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // ==========================================
    // 🏆 NUEVO: PRODUCTOS MÁS VENDIDOS
    // ==========================================
    // Agrupa por producto, suma cantidad y suma subtotal. Ordena desc.
    @Query("SELECT d.producto.nombre, SUM(d.cantidad), SUM(d.subtotal) " +
            "FROM Venta v JOIN v.detalles d " +
            "WHERE v.estado = 'COMPLETADA' " +
            "GROUP BY d.producto.id, d.producto.nombre " +
            "ORDER BY SUM(d.cantidad) DESC")
    List<Object[]> obtenerTopProductos(Pageable pageable);
}