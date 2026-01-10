package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.response.DashboardResponseDTO;
import com.upc.smaf.dtos.VentasSemanaDTO;
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

    /**
     * Obtiene el total de ventas de hoy
     */
    BigDecimal obtenerVentasHoy();
    List<VentasSemanaDTO> obtenerVentasSemanaActual();

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
    List<ProductoVendidoDTO> obtenerProductosMasVendidos(int limit);

}