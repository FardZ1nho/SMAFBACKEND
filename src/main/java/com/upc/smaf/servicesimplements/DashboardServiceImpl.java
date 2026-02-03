package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.GraficoVentasDTO;
import com.upc.smaf.dtos.response.DashboardAlertaDTO;
import com.upc.smaf.dtos.response.DashboardResponseDTO;
import com.upc.smaf.dtos.response.ProductoVendidoDTO;
import com.upc.smaf.entities.Compra;
import com.upc.smaf.entities.Importacion;
import com.upc.smaf.repositories.*;
import com.upc.smaf.serviceinterface.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ImportacionRepository importacionRepository;
    private final CompraRepository compraRepository;
    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;

    // ==========================================
    // 1. MÉTRICAS GENERALES
    // ==========================================
    @Override
    public DashboardResponseDTO obtenerMetricasDashboard() {
        DashboardResponseDTO dashboard = new DashboardResponseDTO();

        LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
        LocalDateTime finHoy = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMes = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime inicioMesAnt = LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMesAnt = inicioMesAnt.plusMonths(1).minusSeconds(1);

        BigDecimal ventasHoy = ventaRepository.sumarVentasCompletadasEntreFechas(inicioHoy, finHoy);
        BigDecimal ventasMes = ventaRepository.sumarVentasCompletadasEntreFechas(inicioMes, finMes);
        BigDecimal ventasMesAnt = ventaRepository.sumarVentasCompletadasEntreFechas(inicioMesAnt, finMesAnt);

        dashboard.setVentasHoy(ventasHoy);
        dashboard.setVentasMes(ventasMes);

        Integer cantVentasHoy = ventaRepository.contarVentasCompletadasEntreFechas(inicioHoy, finHoy);
        Integer cantVentasMes = ventaRepository.contarVentasCompletadasEntreFechas(inicioMes, finMes);

        dashboard.setCantidadVentasHoy(cantVentasHoy != null ? cantVentasHoy : 0);
        dashboard.setCantidadVentasMes(cantVentasMes != null ? cantVentasMes : 0);

        dashboard.setClientesActivos(clienteRepository.count());
        dashboard.setProductosStock((int) productoRepository.count());

        BigDecimal valorInv = productoRepository.findAll().stream()
                .map(p -> {
                    BigDecimal costo = p.getCostoTotal() != null ? p.getCostoTotal() : BigDecimal.ZERO;
                    BigDecimal stock = new BigDecimal(p.getStockActual() != null ? p.getStockActual() : 0);
                    return costo.multiply(stock);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dashboard.setValorInventario(valorInv);

        dashboard.setPorcentajeCambioVentasMes(calcularVariacion(ventasMesAnt, ventasMes));
        dashboard.setPorcentajeCambioClientes(0.0);
        dashboard.setPorcentajeCambioProductos(0.0);
        dashboard.setPorcentajeCambioVentasHoy(0.0);

        return dashboard;
    }

    private double calcularVariacion(BigDecimal anterior, BigDecimal actual) {
        if (anterior == null || anterior.compareTo(BigDecimal.ZERO) == 0) return 100.0;
        if (actual == null) actual = BigDecimal.ZERO;
        BigDecimal diff = actual.subtract(anterior);
        return diff.divide(anterior, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).doubleValue();
    }

    // ==========================================
    // 2. MÉTODOS SIMPLES
    // ==========================================
    @Override
    public BigDecimal obtenerVentasMesActual() {
        LocalDateTime inicio = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return ventaRepository.sumarVentasCompletadasEntreFechas(inicio, LocalDateTime.now());
    }

    @Override
    public BigDecimal obtenerVentasHoy() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        return ventaRepository.sumarVentasCompletadasEntreFechas(inicio, LocalDateTime.now());
    }

    @Override
    public Integer obtenerTotalProductosActivos() {
        return (int) productoRepository.count();
    }

    @Override
    public Long obtenerClientesActivos() {
        return clienteRepository.count();
    }

    // ==========================================
    // 3. GRÁFICOS DINÁMICOS (LUNES-DOMINGO FIJO)
    // ==========================================
    @Override
    public List<GraficoVentasDTO> obtenerVentasGrafico(String periodo) {
        LocalDateTime fin = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime inicio;
        List<Object[]> resultadosRaw;
        List<GraficoVentasDTO> grafico = new ArrayList<>();

        if ("SEMANA".equalsIgnoreCase(periodo)) {
            // Lunes de esta semana
            LocalDate hoy = LocalDate.now();
            LocalDate lunesEstaSemana = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            inicio = lunesEstaSemana.atStartOfDay();
            LocalDate domingoEstaSemana = lunesEstaSemana.plusDays(6);
            LocalDateTime finSemana = domingoEstaSemana.atTime(LocalTime.MAX);

            resultadosRaw = ventaRepository.obtenerVentasPorDiaRaw(inicio, finSemana);

            var mapVentas = resultadosRaw.stream().collect(Collectors.toMap(
                    row -> row[0].toString(),
                    row -> row
            ));

            DateTimeFormatter diaFormatter = DateTimeFormatter.ofPattern("EEEE", new Locale("es", "ES"));

            for (int i = 0; i < 7; i++) {
                LocalDate fecha = lunesEstaSemana.plusDays(i);
                String keyFecha = fecha.toString();

                BigDecimal total = BigDecimal.ZERO;
                Long cantidad = 0L;

                if (mapVentas.containsKey(keyFecha)) {
                    Object[] data = mapVentas.get(keyFecha);
                    total = (BigDecimal) data[1];
                    cantidad = ((Number) data[2]).longValue();
                }

                String label = fecha.format(diaFormatter).toUpperCase();
                grafico.add(new GraficoVentasDTO(label, total, cantidad));
            }

        } else if ("MES".equalsIgnoreCase(periodo)) {
            inicio = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            resultadosRaw = ventaRepository.obtenerVentasPorDiaRaw(inicio, fin);

            BigDecimal[] totalesSemana = {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
            long[] countsSemana = {0, 0, 0, 0};

            for (Object[] row : resultadosRaw) {
                Date fechaSql = (Date) row[0];
                LocalDate fecha = fechaSql.toLocalDate();
                BigDecimal total = (BigDecimal) row[1];
                Long cant = ((Number) row[2]).longValue();

                int dia = fecha.getDayOfMonth();
                int index = (dia <= 7) ? 0 : (dia <= 14) ? 1 : (dia <= 21) ? 2 : 3;

                totalesSemana[index] = totalesSemana[index].add(total);
                countsSemana[index] += cant;
            }

            for (int i = 0; i < 4; i++) {
                grafico.add(new GraficoVentasDTO("S" + (i + 1), totalesSemana[i], countsSemana[i]));
            }

        } else if ("ANIO".equalsIgnoreCase(periodo)) {
            inicio = LocalDate.now().withDayOfYear(1).atStartOfDay();
            resultadosRaw = ventaRepository.obtenerVentasPorMesRaw(inicio, fin);

            var mapMeses = new java.util.HashMap<Integer, Object[]>();
            for (Object[] row : resultadosRaw) {
                try {
                    int mes = Integer.parseInt(row[0].toString());
                    mapMeses.put(mes, row);
                } catch (Exception e) { continue; }
            }

            String[] labelsMeses = {"ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC"};

            for (int i = 1; i <= 12; i++) {
                BigDecimal total = BigDecimal.ZERO;
                Long cantidad = 0L;

                if (mapMeses.containsKey(i)) {
                    Object[] data = mapMeses.get(i);
                    total = (BigDecimal) data[1];
                    cantidad = ((Number) data[2]).longValue();
                }

                grafico.add(new GraficoVentasDTO(labelsMeses[i - 1], total, cantidad));
            }
        }

        return grafico;
    }

    @Override
    public List<GraficoVentasDTO> obtenerVentasSemanaActual() {
        return obtenerVentasGrafico("SEMANA");
    }

    // ==========================================
    // 4. IMPORTACIONES (CORREGIDO: Muestra aunque no tenga fecha)
    // ==========================================
    @Override
    public List<DashboardAlertaDTO> obtenerProximasLlegadas() {
        List<Importacion> lista = importacionRepository.findAll().stream()
                .filter(i -> {
                    String estado = i.getEstado().name();
                    boolean tieneFecha = i.getFechaEstimadaLlegada() != null;
                    boolean estaActiva = "EN_TRANSITO".equals(estado) || "EN_ADUANAS".equals(estado);
                    return tieneFecha || estaActiva;
                })
                .filter(i -> !"COMPLETADA".equals(i.getEstado().name()) && !"CANCELADA".equals(i.getEstado().name()))
                .sorted((a, b) -> {
                    if (a.getFechaEstimadaLlegada() == null) return 1;
                    if (b.getFechaEstimadaLlegada() == null) return -1;
                    return a.getFechaEstimadaLlegada().compareTo(b.getFechaEstimadaLlegada());
                })
                .limit(5)
                .collect(Collectors.toList());

        return lista.stream().map(i -> {
            DashboardAlertaDTO dto = new DashboardAlertaDTO();
            dto.setIdImportacion(i.getId());
            dto.setCodigoImportacion(i.getCodigoAgrupador());
            dto.setFechaLlegada(i.getFechaEstimadaLlegada());
            dto.setEstado(i.getEstado().name());

            List<Compra> facturas = compraRepository.findByCodImportacion(i.getCodigoAgrupador());
            if (facturas != null && !facturas.isEmpty()) {
                String provs = facturas.stream()
                        .filter(c -> c.getProveedor() != null)
                        .map(c -> c.getProveedor().getNombre())
                        .distinct()
                        .collect(Collectors.joining(", "));
                dto.setProveedores(provs.isEmpty() ? "Sin Proveedor" : provs);
            } else {
                dto.setProveedores("Sin Facturas");
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public Double calcularPorcentajeCambioVentas() {
        return obtenerMetricasDashboard().getPorcentajeCambioVentasMes();
    }

    @Override
    public Integer obtenerProductosStockBajo() {
        return (int) productoRepository.findAll().stream()
                .filter(p -> (p.getStockActual() == null ? 0 : p.getStockActual()) < 5)
                .count();
    }

    // ==========================================
    // 🏆 5. PRODUCTOS MÁS VENDIDOS (CORREGIDO)
    // ==========================================
    @Override
    public List<ProductoVendidoDTO> obtenerProductosMasVendidos(int limit) {
        // Pedimos los 'limit' (ej: 5) productos con más ventas
        Pageable pageable = PageRequest.of(0, limit);

        // Llamada a la query que agregamos en VentaRepository
        List<Object[]> resultados = ventaRepository.obtenerTopProductos(pageable);

        return resultados.stream().map(fila -> {
            String nombre = (String) fila[0];
            Number cantidad = (Number) fila[1]; // Evita error Long/Integer
            BigDecimal total = (BigDecimal) fila[2];

            ProductoVendidoDTO dto = new ProductoVendidoDTO();
            dto.setNombreProducto(nombre);
            dto.setCantidad(cantidad.longValue());
            dto.setTotal(total);
            return dto;
        }).collect(Collectors.toList());
    }
}