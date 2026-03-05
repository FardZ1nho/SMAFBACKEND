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
    private BigDecimal ventasMes;
    private BigDecimal ventasHoy;
    private Integer productosStock;
    private Long clientesActivos;

    // ========== PORCENTAJES DE CAMBIO ==========
    private Double porcentajeCambioVentasMes;
    private Double porcentajeCambioProductos;
    private Double porcentajeCambioClientes;
    private Double porcentajeCambioVentasHoy;

    // ========== INFORMACIÓN ADICIONAL ==========
    private Integer cantidadVentasHoy;
    private Integer cantidadVentasMes;
    private BigDecimal valorInventario;

    // ✅ NUEVO: ALERTAS Y TAREAS PENDIENTES
    private Integer productosStockBajo;
    private Integer cotizacionesPendientes; // Para saber cuántas cotizaciones cerrar
    private Integer comprasPorPagar; // Facturas pendientes a proveedores

    // ✅ NUEVO: LIQUIDEZ Y FINANZAS
    private BigDecimal saldoCajaChica;
    private BigDecimal saldoBancos;
}