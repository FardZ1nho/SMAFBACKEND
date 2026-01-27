package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.GraficoVentasDTO;
import com.upc.smaf.dtos.response.DashboardAlertaDTO;
import com.upc.smaf.dtos.response.DashboardResponseDTO;
import com.upc.smaf.dtos.response.ProductoVendidoDTO;
import com.upc.smaf.entities.Importacion;
import com.upc.smaf.repositories.*;
import com.upc.smaf.serviceinterface.DashboardService;
import lombok.RequiredArgsConstructor;
// ✅ CORRECCIÓN DE IMPORT: Usar Spring Data
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ImportacionRepository importacionRepository;

    private final Locale SPANISH_LOCALE = new Locale("es", "ES");

    @Override
    public DashboardResponseDTO obtenerMetricasDashboard() {
        DashboardResponseDTO dashboard = new DashboardResponseDTO();
        dashboard.setVentasMes(obtenerVentasMesActual());
        dashboard.setVentasHoy(obtenerVentasHoy());
        dashboard.setProductosStock(obtenerTotalProductosActivos());
        dashboard.setClientesActivos(obtenerClientesActivos());
        dashboard.setPorcentajeCambioVentasMes(calcularPorcentajeCambioVentas());
        dashboard.setPorcentajeCambioProductos(calcularPorcentajeCambioProductos());
        dashboard.setPorcentajeCambioClientes(calcularPorcentajeCambioClientes());
        dashboard.setPorcentajeCambioVentasHoy(calcularPorcentajeCambioVentasHoy());
        dashboard.setProductosStockBajo(obtenerProductosStockBajo());
        dashboard.setCantidadVentasHoy(contarVentasHoy());
        dashboard.setCantidadVentasMes(contarVentasMes());
        dashboard.setValorInventario(obtenerValorInventario());
        return dashboard;
    }

    @Override
    public List<DashboardAlertaDTO> obtenerProximasLlegadas() {
        // Pedimos solo las top 5 más cercanas
        Pageable top5 = PageRequest.of(0, 5);

        // ✅ CORRECCIÓN: Usar la instancia 'importacionRepository' (minúscula)
        List<Importacion> lista = importacionRepository.findProximasLlegadas(top5);

        return lista.stream().map(i -> {
            DashboardAlertaDTO dto = new DashboardAlertaDTO();
            dto.setIdImportacion(i.getId());

            if (i.getCompra() != null) {
                dto.setCodigoImportacion(i.getCompra().getSerie() + "-" + i.getCompra().getNumero());
                if (i.getCompra().getProveedor() != null) {
                    dto.setProveedor(i.getCompra().getProveedor().getNombre());
                }
            }

            // ✅ CORRECCIÓN: Usar el getter getFechaEstimadaLlegada()
            dto.setFechaEta(i.getFechaEstimadaLlegada());
            dto.setEstado(i.getEstado().name());

            // Calcular días restantes
            if (i.getFechaEstimadaLlegada() != null) {
                long dias = java.time.temporal.ChronoUnit.DAYS.between(
                        java.time.LocalDate.now(),
                        i.getFechaEstimadaLlegada()
                );
                dto.setDiasRestantes(dias);
            } else {
                dto.setDiasRestantes(0L); // Fallback si es nula
            }

            return dto;
        }).collect(Collectors.toList());
    }

    // ==========================================
    // 📈 LÓGICA GRÁFICO
    // ==========================================
    @Override
    public List<GraficoVentasDTO> obtenerVentasGrafico(String periodo) {
        LocalDateTime hoy = LocalDateTime.now();
        List<GraficoVentasDTO> resultadoFinal = new ArrayList<>();

        if ("ANIO".equals(periodo)) {
            // --- Lógica AÑO (12 Meses) ---
            LocalDateTime inicio = LocalDate.now().withDayOfYear(1).atStartOfDay();
            List<Object[]> dataBD = ventaRepository.obtenerVentasPorMesRaw(inicio, hoy);

            Map<Integer, GraficoVentasDTO> mapaMeses = new HashMap<>();
            for (Object[] fila : dataBD) {
                int mes = Integer.parseInt((String) fila[0]);
                BigDecimal total = (BigDecimal) fila[1];
                Long cant = ((Number) fila[2]).longValue();
                mapaMeses.put(mes, new GraficoVentasDTO("", total, cant));
            }

            for (int i = 1; i <= 12; i++) {
                String nombreMes = Month.of(i).getDisplayName(TextStyle.FULL, SPANISH_LOCALE);
                nombreMes = nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1);

                if (mapaMeses.containsKey(i)) {
                    GraficoVentasDTO dto = mapaMeses.get(i);
                    dto.setLabel(nombreMes);
                    resultadoFinal.add(dto);
                } else {
                    resultadoFinal.add(new GraficoVentasDTO(nombreMes, BigDecimal.ZERO, 0L));
                }
            }

        } else if ("MES".equals(periodo)) {
            // --- LÓGICA MES (Agrupado en 4 Semanas) ---
            LocalDateTime inicio = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            List<Object[]> dataBD = ventaRepository.obtenerVentasPorDiaRaw(inicio, hoy);

            Map<Integer, GraficoVentasDTO> semanasMap = new TreeMap<>();
            semanasMap.put(1, new GraficoVentasDTO("Semana 1", BigDecimal.ZERO, 0L));
            semanasMap.put(2, new GraficoVentasDTO("Semana 2", BigDecimal.ZERO, 0L));
            semanasMap.put(3, new GraficoVentasDTO("Semana 3", BigDecimal.ZERO, 0L));
            semanasMap.put(4, new GraficoVentasDTO("Semana 4", BigDecimal.ZERO, 0L));

            for (Object[] fila : dataBD) {
                LocalDate fecha = ((Date) fila[0]).toLocalDate();
                BigDecimal total = (BigDecimal) fila[1];
                Long cant = ((Number) fila[2]).longValue();
                int dia = fecha.getDayOfMonth();

                int semanaIdx;
                if (dia <= 7) semanaIdx = 1;
                else if (dia <= 14) semanaIdx = 2;
                else if (dia <= 21) semanaIdx = 3;
                else semanaIdx = 4;

                GraficoVentasDTO dto = semanasMap.get(semanaIdx);
                dto.setTotal(dto.getTotal().add(total));
                dto.setCantidad(dto.getCantidad() + cant);
            }

            resultadoFinal.addAll(semanasMap.values());

        } else {
            // --- LÓGICA SEMANA (Lunes a Domingo) ---
            LocalDateTime inicio = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
            List<Object[]> dataBD = ventaRepository.obtenerVentasPorDiaRaw(inicio, hoy);

            Map<LocalDate, GraficoVentasDTO> mapaVentas = new HashMap<>();
            for (Object[] fila : dataBD) {
                LocalDate fecha = ((Date) fila[0]).toLocalDate();
                BigDecimal total = (BigDecimal) fila[1];
                Long cant = ((Number) fila[2]).longValue();
                mapaVentas.put(fecha, new GraficoVentasDTO("", total, cant));
            }

            LocalDate fechaIterador = inicio.toLocalDate();
            for (int i = 0; i < 7; i++) {
                String diaNombre = fechaIterador.getDayOfWeek().getDisplayName(TextStyle.FULL, SPANISH_LOCALE);
                diaNombre = diaNombre.substring(0, 1).toUpperCase() + diaNombre.substring(1);

                if (mapaVentas.containsKey(fechaIterador)) {
                    GraficoVentasDTO dto = mapaVentas.get(fechaIterador);
                    dto.setLabel(diaNombre);
                    resultadoFinal.add(dto);
                } else {
                    resultadoFinal.add(new GraficoVentasDTO(diaNombre, BigDecimal.ZERO, 0L));
                }
                fechaIterador = fechaIterador.plusDays(1);
            }
        }

        return resultadoFinal;
    }

    @Override
    public List<GraficoVentasDTO> obtenerVentasSemanaActual() {
        return obtenerVentasGrafico("SEMANA");
    }

    // Métodos auxiliares
    @Override public BigDecimal obtenerVentasMesActual() { LocalDateTime i = LocalDate.now().withDayOfMonth(1).atStartOfDay(); LocalDateTime f = LocalDate.now().atTime(LocalTime.MAX); BigDecimal t = ventaRepository.sumarVentasCompletadasEntreFechas(i, f); return t!=null?t:BigDecimal.ZERO; }
    @Override public BigDecimal obtenerVentasHoy() { LocalDateTime i = LocalDate.now().atStartOfDay(); LocalDateTime f = LocalDate.now().atTime(LocalTime.MAX); BigDecimal t = ventaRepository.sumarVentasCompletadasEntreFechas(i, f); return t!=null?t:BigDecimal.ZERO; }
    @Override public Integer obtenerTotalProductosActivos() { Integer t = productoRepository.contarProductosActivos(); return t!=null?t:0; }
    @Override public Long obtenerClientesActivos() { Long t = clienteRepository.contarClientesActivos(); return t!=null?t:0L; }
    @Override public Integer obtenerProductosStockBajo() { Integer t = productoRepository.contarProductosStockBajo(); return t!=null?t:0; }
    @Override public Double calcularPorcentajeCambioVentas() { return 0.0; }
    @Override public List<ProductoVendidoDTO> obtenerProductosMasVendidos(int limit) { return detalleVentaRepository.findProductosMasVendidos(limit).stream().limit(limit).collect(Collectors.toList()); }

    private Integer contarVentasHoy() { return 0; }
    private Integer contarVentasMes() { return 0; }
    private BigDecimal obtenerValorInventario() { return BigDecimal.ZERO; }
    private Double calcularPorcentajeCambioProductos() { return 0.0; }
    private Double calcularPorcentajeCambioClientes() { return 0.0; }
    private Double calcularPorcentajeCambioVentasHoy() { return 0.0; }
}