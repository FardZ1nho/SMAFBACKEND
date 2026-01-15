package com.upc.smaf.repositories;

import com.upc.smaf.entities.Venta;
import com.upc.smaf.entities.EstadoVenta;
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

    // ==========================================
    // ✅ NUEVOS MÉTODOS AGREGADOS (Para Documentos)
    // ==========================================

    // Buscar una venta específica por su número de comprobante (Ej: "F001-00025")
    Optional<Venta> findByNumeroDocumento(String numeroDocumento);

    // Verificar si ya existe un número de documento (Útil para validar duplicados antes de guardar)
    boolean existsByNumeroDocumento(String numeroDocumento);

    // Filtrar por tipo (Ej: Traer todas las "FACTURA" o "BOLETA")
    List<Venta> findByTipoDocumento(String tipoDocumento);

    // ==========================================
    // FIN NUEVOS MÉTODOS
    // ==========================================

    // ========== ESTADÍSTICAS Y REPORTES ==========

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin")
    BigDecimal sumarVentasEntreFechas(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin")
    Integer contarVentasEntreFechas(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin AND v.estado = 'COMPLETADA'")
    BigDecimal sumarVentasCompletadasEntreFechas(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin AND v.estado = 'COMPLETADA'")
    Integer contarVentasCompletadasEntreFechas(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query(value =
            "SELECT " +
                    "   DATE(v.fecha_venta) as fecha, " +
                    "   TO_CHAR(v.fecha_venta, 'Day') as diaSemana, " +
                    "   COALESCE(SUM(v.total), 0) as totalVentas, " +
                    "   COUNT(v.id) as cantidadVentas " +
                    "FROM ventas v " +
                    "WHERE v.fecha_venta >= :inicioSemana " +
                    "AND v.fecha_venta < :finSemana " +
                    "AND v.estado = 'COMPLETADA' " +
                    "GROUP BY DATE(v.fecha_venta), TO_CHAR(v.fecha_venta, 'Day') " +
                    "ORDER BY DATE(v.fecha_venta)",
            nativeQuery = true)
    List<Object[]> obtenerVentasPorDiaSemanaRaw(
            @Param("inicioSemana") LocalDateTime inicioSemana,
            @Param("finSemana") LocalDateTime finSemana
    );
}