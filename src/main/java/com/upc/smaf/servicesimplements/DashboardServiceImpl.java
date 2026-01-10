package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.response.DashboardResponseDTO;
import com.upc.smaf.dtos.VentasSemanaDTO;
import com.upc.smaf.dtos.response.ProductoVendidoDTO;
import com.upc.smaf.repositories.ClienteRepository;
import com.upc.smaf.repositories.DetalleVentaRepository;
import com.upc.smaf.repositories.ProductoRepository;
import com.upc.smaf.repositories.VentaRepository;
import com.upc.smaf.serviceinterface.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    @Override
    public DashboardResponseDTO obtenerMetricasDashboard() {
        DashboardResponseDTO dashboard = new DashboardResponseDTO();

        // Métricas principales
        dashboard.setVentasMes(obtenerVentasMesActual());
        dashboard.setVentasHoy(obtenerVentasHoy());
        dashboard.setProductosStock(obtenerTotalProductosActivos());
        dashboard.setClientesActivos(obtenerClientesActivos());

        // Porcentajes de cambio
        dashboard.setPorcentajeCambioVentasMes(calcularPorcentajeCambioVentas());
        dashboard.setPorcentajeCambioProductos(calcularPorcentajeCambioProductos());
        dashboard.setPorcentajeCambioClientes(calcularPorcentajeCambioClientes());
        dashboard.setPorcentajeCambioVentasHoy(calcularPorcentajeCambioVentasHoy());

        // Información adicional
        dashboard.setProductosStockBajo(obtenerProductosStockBajo());
        dashboard.setCantidadVentasHoy(contarVentasHoy());
        dashboard.setCantidadVentasMes(contarVentasMes());
        dashboard.setValorInventario(obtenerValorInventario());

        return dashboard;
    }

    @Override
    public List<VentasSemanaDTO> obtenerVentasSemanaActual() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemana = hoy.with(DayOfWeek.MONDAY);
        LocalDate finSemana = hoy.with(DayOfWeek.SUNDAY).plusDays(1);

        LocalDateTime inicioSemanaDateTime = inicioSemana.atStartOfDay();
        LocalDateTime finSemanaDateTime = finSemana.atStartOfDay();

        // Obtener resultados crudos
        List<Object[]> resultados = ventaRepository.obtenerVentasPorDiaSemanaRaw(
                inicioSemanaDateTime, finSemanaDateTime
        );

        // Convertir a DTO manualmente
        Map<LocalDate, VentasSemanaDTO> ventasMap = new HashMap<>();
        for (Object[] row : resultados) {
            LocalDate fecha = ((java.sql.Date) row[0]).toLocalDate();
            String diaSemana = (String) row[1];
            BigDecimal totalVentas = (BigDecimal) row[2];
            Long cantidadVentas = ((Number) row[3]).longValue();

            VentasSemanaDTO dto = new VentasSemanaDTO(fecha, diaSemana, totalVentas, cantidadVentas);
            ventasMap.put(fecha, dto);
        }

        // Llenar todos los días de la semana
        List<VentasSemanaDTO> resultado = new ArrayList<>();
        String[] diasSemana = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};

        for (int i = 0; i < 7; i++) {
            LocalDate fecha = inicioSemana.plusDays(i);
            VentasSemanaDTO venta = ventasMap.getOrDefault(fecha,
                    new VentasSemanaDTO(fecha, diasSemana[i], BigDecimal.ZERO, 0L)
            );
            resultado.add(venta);
        }

        return resultado;
    }

    @Override
    public BigDecimal obtenerVentasMesActual() {
        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMes = LocalDate.now().atTime(LocalTime.MAX);

        BigDecimal total = ventaRepository.sumarVentasCompletadasEntreFechas(inicioMes, finMes);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal obtenerVentasHoy() {
        LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
        LocalDateTime finHoy = LocalDate.now().atTime(LocalTime.MAX);

        BigDecimal total = ventaRepository.sumarVentasCompletadasEntreFechas(inicioHoy, finHoy);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public Integer obtenerTotalProductosActivos() {
        Integer total = productoRepository.contarProductosActivos();
        return total != null ? total : 0;
    }

    @Override
    public Long obtenerClientesActivos() {
        Long total = clienteRepository.contarClientesActivos();
        return total != null ? total : 0L;
    }

    @Override
    public Double calcularPorcentajeCambioVentas() {
        // Ventas del mes actual
        BigDecimal ventasMesActual = obtenerVentasMesActual();

        // Ventas del mes anterior
        YearMonth mesAnterior = YearMonth.now().minusMonths(1);
        LocalDateTime inicioMesAnterior = mesAnterior.atDay(1).atStartOfDay();
        LocalDateTime finMesAnterior = mesAnterior.atEndOfMonth().atTime(LocalTime.MAX);

        BigDecimal ventasMesAnterior = ventaRepository.sumarVentasCompletadasEntreFechas(
                inicioMesAnterior, finMesAnterior
        );

        if (ventasMesAnterior == null || ventasMesAnterior.compareTo(BigDecimal.ZERO) == 0) {
            return ventasMesActual.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }

        // Calcular porcentaje: ((actual - anterior) / anterior) * 100
        BigDecimal diferencia = ventasMesActual.subtract(ventasMesAnterior);
        BigDecimal porcentaje = diferencia
                .divide(ventasMesAnterior, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        return porcentaje.doubleValue();
    }

    @Override
    public Integer obtenerProductosStockBajo() {
        Integer total = productoRepository.contarProductosStockBajo();
        return total != null ? total : 0;
    }

    @Override
    public List<ProductoVendidoDTO> obtenerProductosMasVendidos(int limit) {
        List<ProductoVendidoDTO> productos = detalleVentaRepository.findProductosMasVendidos(limit);
        return productos.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ========== MÉTODOS AUXILIARES ==========

    private Double calcularPorcentajeCambioProductos() {
        // Por ahora retornamos 0, pero puedes implementar lógica comparando con mes anterior
        // Para eso necesitarías guardar histórico de productos o comparar con fechaCreacion
        return 0.0;
    }

    private Double calcularPorcentajeCambioClientes() {
        // Similar a productos, necesitarías histórico
        return 0.0;
    }

    private Double calcularPorcentajeCambioVentasHoy() {
        BigDecimal ventasHoy = obtenerVentasHoy();

        // Ventas de ayer
        LocalDateTime inicioAyer = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime finAyer = LocalDate.now().minusDays(1).atTime(LocalTime.MAX);

        BigDecimal ventasAyer = ventaRepository.sumarVentasCompletadasEntreFechas(inicioAyer, finAyer);

        if (ventasAyer == null || ventasAyer.compareTo(BigDecimal.ZERO) == 0) {
            return ventasHoy.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }

        BigDecimal diferencia = ventasHoy.subtract(ventasAyer);
        BigDecimal porcentaje = diferencia
                .divide(ventasAyer, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        return porcentaje.doubleValue();
    }

    private Integer contarVentasHoy() {
        LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
        LocalDateTime finHoy = LocalDate.now().atTime(LocalTime.MAX);

        Integer total = ventaRepository.contarVentasCompletadasEntreFechas(inicioHoy, finHoy);
        return total != null ? total : 0;
    }

    private Integer contarVentasMes() {
        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMes = LocalDate.now().atTime(LocalTime.MAX);

        Integer total = ventaRepository.contarVentasCompletadasEntreFechas(inicioMes, finMes);
        return total != null ? total : 0;
    }

    private BigDecimal obtenerValorInventario() {
        BigDecimal valor = productoRepository.getValorTotalInventario();
        return valor != null ? valor : BigDecimal.ZERO;
    }
}