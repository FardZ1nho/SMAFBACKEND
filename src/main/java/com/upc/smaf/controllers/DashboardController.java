package com.upc.smaf.controllers;

import com.upc.smaf.dtos.response.DashboardResponseDTO;
import com.upc.smaf.dtos.VentasSemanaDTO;
import com.upc.smaf.dtos.response.ProductoVendidoDTO;
import com.upc.smaf.serviceinterface.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Obtiene todas las métricas del dashboard en una sola llamada
     * GET /dashboard/metricas
     */
    @GetMapping("/metricas")
    public ResponseEntity<DashboardResponseDTO> obtenerMetricas() {
        DashboardResponseDTO metricas = dashboardService.obtenerMetricasDashboard();
        return ResponseEntity.ok(metricas);
    }

    /**
     * Obtiene las ventas del mes actual
     * GET /dashboard/ventas-mes
     */
    @GetMapping("/ventas-mes")
    public ResponseEntity<BigDecimal> obtenerVentasMes() {
        BigDecimal ventasMes = dashboardService.obtenerVentasMesActual();
        return ResponseEntity.ok(ventasMes);
    }

    /**
     * Obtiene los productos más vendidos
     * GET /dashboard/productos-mas-vendidos?limit=5
     */
    @GetMapping("/productos-mas-vendidos")
    public ResponseEntity<List<ProductoVendidoDTO>> obtenerProductosMasVendidos(
            @RequestParam(defaultValue = "5") int limit) {
        List<ProductoVendidoDTO> productos = dashboardService.obtenerProductosMasVendidos(limit);
        return ResponseEntity.ok(productos);
    }

    /**
     * Obtiene las ventas de hoy
     * GET /dashboard/ventas-hoy
     */
    @GetMapping("/ventas-hoy")
    public ResponseEntity<BigDecimal> obtenerVentasHoy() {
        BigDecimal ventasHoy = dashboardService.obtenerVentasHoy();
        return ResponseEntity.ok(ventasHoy);
    }

    /**
     * Obtiene la cantidad total de productos en stock
     * GET /dashboard/productos-stock
     */
    @GetMapping("/productos-stock")
    public ResponseEntity<Integer> obtenerProductosEnStock() {
        Integer productosStock = dashboardService.obtenerTotalProductosActivos();
        return ResponseEntity.ok(productosStock);
    }

    /**
     * Obtiene la cantidad de clientes activos
     * GET /dashboard/clientes-activos
     */
    @GetMapping("/clientes-activos")
    public ResponseEntity<Long> obtenerClientesActivos() {
        Long clientesActivos = dashboardService.obtenerClientesActivos();
        return ResponseEntity.ok(clientesActivos);
    }

    /**
     * Obtiene el porcentaje de cambio de ventas vs mes anterior
     * GET /dashboard/porcentaje-ventas
     */
    @GetMapping("/porcentaje-ventas")
    public ResponseEntity<Double> obtenerPorcentajeCambioVentas() {
        Double porcentaje = dashboardService.calcularPorcentajeCambioVentas();
        return ResponseEntity.ok(porcentaje);
    }



    @GetMapping("/ventas-semana")
    public ResponseEntity<List<VentasSemanaDTO>> obtenerVentasSemanaActual() {
        List<VentasSemanaDTO> ventasSemana = dashboardService.obtenerVentasSemanaActual();
        return ResponseEntity.ok(ventasSemana);
    }
}