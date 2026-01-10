package com.upc.smaf.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDTO {

    // ========== MÉTRICAS PRINCIPALES ==========

    /**
     * Total de ventas del mes actual en soles
     */
    private BigDecimal ventasMes;

    /**
     * Total de ventas de hoy en soles
     */
    private BigDecimal ventasHoy;

    /**
     * Cantidad total de productos activos
     */
    private Integer productosStock;

    /**
     * Cantidad de clientes activos
     */
    private Long clientesActivos;

    // ========== PORCENTAJES DE CAMBIO ==========

    /**
     * Porcentaje de cambio vs mes anterior
     * Positivo = aumento, Negativo = disminución
     */
    private Double porcentajeCambioVentasMes;

    /**
     * Porcentaje de cambio de productos vs mes anterior
     */
    private Double porcentajeCambioProductos;

    /**
     * Porcentaje de cambio de clientes vs mes anterior
     */
    private Double porcentajeCambioClientes;

    /**
     * Porcentaje de cambio de ventas hoy vs ayer
     */
    private Double porcentajeCambioVentasHoy;

    // ========== INFORMACIÓN ADICIONAL ==========

    /**
     * Cantidad de productos con stock bajo (menor al mínimo)
     */
    private Integer productosStockBajo;

    /**
     * Cantidad de ventas realizadas hoy (número de transacciones)
     */
    private Integer cantidadVentasHoy;

    /**
     * Cantidad de ventas del mes (número de transacciones)
     */
    private Integer cantidadVentasMes;

    /**
     * Valor total del inventario (suma de stock * costo)
     */
    private BigDecimal valorInventario;
}