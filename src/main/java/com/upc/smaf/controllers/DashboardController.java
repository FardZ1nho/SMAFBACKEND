package com.upc.smaf.controllers;

import com.upc.smaf.dtos.GraficoVentasDTO;
import com.upc.smaf.dtos.ReporteMetodoPagoDTO;
import com.upc.smaf.dtos.response.DashboardAlertaDTO; // ✅ IMPORTAR ESTO
import com.upc.smaf.dtos.response.DashboardResponseDTO;
import com.upc.smaf.dtos.response.ProductoVendidoDTO;
import com.upc.smaf.repositories.VentaRepository;
import com.upc.smaf.serviceinterface.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final VentaRepository ventaRepository;

    /**
     * ✅ Endpoint PRINCIPAL para el gráfico dinámico.
     * Soporta filtros: ?periodo=SEMANA, ?periodo=MES, ?periodo=ANIO
     */
    @GetMapping("/ventas-grafico")
    public ResponseEntity<List<GraficoVentasDTO>> obtenerVentasGrafico(
            @RequestParam(defaultValue = "SEMANA") String periodo) {
        List<GraficoVentasDTO> datos = dashboardService.obtenerVentasGrafico(periodo);
        return ResponseEntity.ok(datos);
    }

    /**
     * Endpoint de compatibilidad (Legacy).
     */
    @GetMapping("/ventas-semana")
    public ResponseEntity<List<GraficoVentasDTO>> obtenerVentasSemana() {
        return ResponseEntity.ok(dashboardService.obtenerVentasGrafico("SEMANA"));
    }

    // ==========================================
    // MÉTRICAS GENERALES Y KPI (Tarjetas Superiores)
    // ==========================================

    @GetMapping("/metricas")
    public ResponseEntity<DashboardResponseDTO> obtenerMetricas() {
        return ResponseEntity.ok(dashboardService.obtenerMetricasDashboard());
    }

    @GetMapping("/ventas-mes")
    public ResponseEntity<BigDecimal> obtenerVentasMes() {
        return ResponseEntity.ok(dashboardService.obtenerVentasMesActual());
    }

    @GetMapping("/ventas-hoy")
    public ResponseEntity<BigDecimal> obtenerVentasHoy() {
        return ResponseEntity.ok(dashboardService.obtenerVentasHoy());
    }

    @GetMapping("/productos-stock")
    public ResponseEntity<Integer> obtenerProductosEnStock() {
        return ResponseEntity.ok(dashboardService.obtenerTotalProductosActivos());
    }

    @GetMapping("/clientes-activos")
    public ResponseEntity<Long> obtenerClientesActivos() {
        return ResponseEntity.ok(dashboardService.obtenerClientesActivos());
    }

    @GetMapping("/porcentaje-ventas")
    public ResponseEntity<Double> obtenerPorcentajeCambioVentas() {
        return ResponseEntity.ok(dashboardService.calcularPorcentajeCambioVentas());
    }

    // ==========================================
    // LISTAS Y REPORTES
    // ==========================================

    @GetMapping("/productos-mas-vendidos")
    public ResponseEntity<List<ProductoVendidoDTO>> obtenerProductosMasVendidos(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(dashboardService.obtenerProductosMasVendidos(limit));
    }

    /**
     * ✅ NUEVO ENDPOINT: Próximas Llegadas (Para el Widget de Alertas)
     */
    @GetMapping("/proximas-llegadas")
    public ResponseEntity<List<DashboardAlertaDTO>> obtenerProximasLlegadas() {
        return ResponseEntity.ok(dashboardService.obtenerProximasLlegadas());
    }

    /**
     * Reporte para el Gráfico Circular (Donut) de Métodos de Pago.
     */
    @GetMapping("/metodos-pago")
    public ResponseEntity<List<ReporteMetodoPagoDTO>> obtenerReporteMetodosPago(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        LocalDateTime inicio;
        LocalDateTime fin;

        if (fechaInicio == null) {
            inicio = LocalDate.now().atStartOfDay();
            fin = LocalDate.now().atTime(LocalTime.MAX);
        } else {
            inicio = fechaInicio.atStartOfDay();
            fin = (fechaFin != null) ? fechaFin.atTime(LocalTime.MAX) : fechaInicio.atTime(LocalTime.MAX);
        }

        List<ReporteMetodoPagoDTO> reporte = ventaRepository.obtenerReporteMetodosPago(inicio, fin);
        return ResponseEntity.ok(reporte);
    }
}