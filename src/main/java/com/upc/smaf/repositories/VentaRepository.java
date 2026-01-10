package com.upc.smaf.repositories;

import com.upc.smaf.dtos.VentasSemanaDTO;
import com.upc.smaf.entities.Venta;
import com.upc.smaf.entities.EstadoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.upc.smaf.dtos.VentasSemanaDTO;

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
    List<Venta> buscarPorCliente(String nombre);

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.estado = :estado")
    Long contarPorEstado(EstadoVenta estado);

    // ========== AGREGAR ESTOS MÉTODOS AL FINAL DE VentaRepository ==========

    /**
     * Suma el total de ventas en un rango de fechas
     */
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin")
    BigDecimal sumarVentasEntreFechas(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Cuenta la cantidad de ventas en un rango de fechas
     */
    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin")
    Integer contarVentasEntreFechas(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Suma el total de ventas completadas en un rango de fechas
     */
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin AND v.estado = 'COMPLETADA'")
    BigDecimal sumarVentasCompletadasEntreFechas(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Cuenta ventas completadas en un rango de fechas
     */
    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin AND v.estado = 'COMPLETADA'")
    Integer contarVentasCompletadasEntreFechas(LocalDateTime inicio, LocalDateTime fin);

    @Query(value =
            "SELECT " +
                    "   DATE(v.fecha_venta) as fecha, " +
                    "   TO_CHAR(v.fecha_venta, 'Day') as diaSemana, " + // ✅ Cambiar DAYNAME por TO_CHAR
                    "   COALESCE(SUM(v.total), 0) as totalVentas, " +
                    "   COUNT(v.id) as cantidadVentas " +
                    "FROM ventas v " +
                    "WHERE v.fecha_venta >= :inicioSemana " +
                    "AND v.fecha_venta < :finSemana " +
                    "AND v.estado = 'COMPLETADA' " +
                    "GROUP BY DATE(v.fecha_venta), TO_CHAR(v.fecha_venta, 'Day') " + // ✅ Cambiar aquí también
                    "ORDER BY DATE(v.fecha_venta)",
            nativeQuery = true)
    List<Object[]> obtenerVentasPorDiaSemanaRaw(
            @Param("inicioSemana") LocalDateTime inicioSemana,
            @Param("finSemana") LocalDateTime finSemana
    );
}