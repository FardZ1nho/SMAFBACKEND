package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.response.DashboardAlertaDTO;
import com.upc.smaf.dtos.response.DashboardResponseDTO;
import com.upc.smaf.dtos.GraficoVentasDTO;
import com.upc.smaf.dtos.response.ProductoVendidoDTO;

import java.math.BigDecimal;
import java.util.List;

public interface DashboardService {

    /**
     * Obtiene todas las métricas del dashboard en una sola respuesta
     */
    DashboardResponseDTO obtenerMetricasDashboard();

    /**
     * Obtiene el total de ventas del mes actual
     */
    BigDecimal obtenerVentasMesActual();
    List<DashboardAlertaDTO> obtenerProximasLlegadas(); // <--- AGREGA ESTO
    /**
     * Obtiene el total de ventas de hoy
     */
    BigDecimal obtenerVentasHoy();

    /**
     * @deprecated Usar obtenerVentasGrafico("SEMANA") en su lugar.
     * Se mantiene por compatibilidad si aun se usa en legacy.
     */
    List<GraficoVentasDTO> obtenerVentasSemanaActual();

    /**
     * ✅ NUEVO: Obtiene datos para el gráfico dinámico según el filtro.
     * @param periodo "SEMANA", "MES" o "ANIO"
     */
    List<GraficoVentasDTO> obtenerVentasGrafico(String periodo);

    /**
     * Obtiene la cantidad total de productos activos
     */
    Integer obtenerTotalProductosActivos();

    /**
     * Obtiene la cantidad de clientes activos
     */
    Long obtenerClientesActivos();

    /**
     * Calcula el porcentaje de cambio de ventas vs mes anterior
     */
    Double calcularPorcentajeCambioVentas();

    /**
     * Obtiene la cantidad de productos con stock bajo
     */
    Integer obtenerProductosStockBajo();

    /**
     * Obtiene el top de productos más vendidos
     */
    List<ProductoVendidoDTO> obtenerProductosMasVendidos(int limit);

}