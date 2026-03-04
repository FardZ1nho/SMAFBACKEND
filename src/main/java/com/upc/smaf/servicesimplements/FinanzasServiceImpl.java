package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.response.FinanzasDashboardResponseDTO;
import com.upc.smaf.entities.Compra;
import com.upc.smaf.entities.EstadoCompra;
import com.upc.smaf.entities.Venta;
import com.upc.smaf.entities.MovimientoCaja;
import com.upc.smaf.repositories.CompraRepository;
import com.upc.smaf.repositories.VentaRepository;
import com.upc.smaf.repositories.MovimientoCajaRepository;
import com.upc.smaf.serviceinterface.FinanzasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanzasServiceImpl implements FinanzasService {

    private final CompraRepository compraRepository;
    private final VentaRepository ventaRepository;
    private final MovimientoCajaRepository cajaRepository;

    @Override
    public FinanzasDashboardResponseDTO obtenerDashboardFinanciero(LocalDate fechaInicio, LocalDate fechaFin) {

        // Si no hay fechas, tomamos un rango muy amplio (histórico)
        LocalDateTime inicio = (fechaInicio != null) ? fechaInicio.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime fin = (fechaFin != null) ? fechaFin.atTime(LocalTime.MAX) : LocalDateTime.now();

        FinanzasDashboardResponseDTO dashboard = new FinanzasDashboardResponseDTO();
        List<FinanzasDashboardResponseDTO.TransaccionDTO> transacciones = new ArrayList<>();

        BigDecimal totalIngresos = BigDecimal.ZERO;
        BigDecimal totalEgresos = BigDecimal.ZERO;

        BigDecimal igvVentas = BigDecimal.ZERO;
        BigDecimal igvCompras = BigDecimal.ZERO;
        BigDecimal retenciones = BigDecimal.ZERO;
        BigDecimal detracciones = BigDecimal.ZERO;
        BigDecimal percepciones = BigDecimal.ZERO;

        // ==========================================
        // 1. PROCESAR COMPRAS (EGRESOS)
        // ==========================================
        List<Compra> compras = compraRepository.findAll();

        for (Compra c : compras) {
            if (c.getEstado() == EstadoCompra.ANULADA) continue;

            LocalDateTime fechaCompra = c.getFechaRegistro() != null ? c.getFechaRegistro() : c.getFechaEmision().atStartOfDay();
            if (fechaCompra.isBefore(inicio) || fechaCompra.isAfter(fin)) continue;

            // 🟢 Normalización de Moneda (Llevar todo a Soles para el cálculo de los KPIs)
            BigDecimal tipoCambio = c.getTipoCambio() != null && c.getTipoCambio().compareTo(BigDecimal.ZERO) > 0 ? c.getTipoCambio() : new BigDecimal("3.80");
            boolean esDolar = "USD".equalsIgnoreCase(c.getMoneda());

            BigDecimal montoNormalizado = orZero(c.getTotal());
            BigDecimal igvNormalizado = orZero(c.getIgv());
            BigDecimal retencionNormalizada = orZero(c.getRetencion());
            BigDecimal detraccionNormalizada = orZero(c.getDetraccionMonto());
            BigDecimal percepcionNormalizada = orZero(c.getPercepcion());

            if (esDolar) {
                montoNormalizado = montoNormalizado.multiply(tipoCambio);
                igvNormalizado = igvNormalizado.multiply(tipoCambio);
                retencionNormalizada = retencionNormalizada.multiply(tipoCambio);
                detraccionNormalizada = detraccionNormalizada.multiply(tipoCambio);
                percepcionNormalizada = percepcionNormalizada.multiply(tipoCambio);
            }

            FinanzasDashboardResponseDTO.TransaccionDTO tx = new FinanzasDashboardResponseDTO.TransaccionDTO();
            tx.setFechaHora(fechaCompra);
            tx.setTipo("EGRESO");
            tx.setOrigen("COMPRA");
            tx.setTipoComprobante(c.getTipoComprobante() != null ? c.getTipoComprobante().name() : "OTRO");
            tx.setComprobante(c.getSerie() + "-" + c.getNumero());
            tx.setEntidad(c.getNombreProveedor() != null ? c.getNombreProveedor() : (c.getProveedor() != null ? c.getProveedor().getNombre() : "Proveedor Libre"));
            tx.setMoneda(c.getMoneda());
            tx.setMontoTotal(orZero(c.getTotal())); // Mostramos en tabla la moneda original

            transacciones.add(tx);

            // Sumamos Egresos e Impuestos en SOLES
            totalEgresos = totalEgresos.add(montoNormalizado);
            igvCompras = igvCompras.add(igvNormalizado);
            retenciones = retenciones.add(retencionNormalizada);
            detracciones = detracciones.add(detraccionNormalizada);
            percepciones = percepciones.add(percepcionNormalizada);
        }

        // ==========================================
        // 2. PROCESAR VENTAS (INGRESOS)
        // ==========================================
        List<Venta> ventas = ventaRepository.findAll();

        for (Venta v : ventas) {
            // Verificamos por nombre para evitar error si tu Enum se llama distinto
            if (v.getEstado() != null && v.getEstado().name().equals("ANULADA")) continue;

            LocalDateTime fechaVenta = v.getFechaVenta() != null ? v.getFechaVenta() : LocalDateTime.now();
            if (fechaVenta.isBefore(inicio) || fechaVenta.isAfter(fin)) continue;

            // 🟢 Normalización de Moneda
            BigDecimal tipoCambio = v.getTipoCambio() != null && v.getTipoCambio().compareTo(BigDecimal.ZERO) > 0 ? v.getTipoCambio() : new BigDecimal("3.80");
            boolean esDolar = "USD".equalsIgnoreCase(v.getMoneda());

            BigDecimal montoNormalizado = orZero(v.getTotal());
            BigDecimal igvNormalizado = orZero(v.getIgv());
            BigDecimal retencionNormalizada = orZero(v.getRetencion());
            BigDecimal detraccionNormalizada = orZero(v.getDetraccion());

            if (esDolar) {
                montoNormalizado = montoNormalizado.multiply(tipoCambio);
                igvNormalizado = igvNormalizado.multiply(tipoCambio);
                retencionNormalizada = retencionNormalizada.multiply(tipoCambio);
                detraccionNormalizada = detraccionNormalizada.multiply(tipoCambio);
            }

            FinanzasDashboardResponseDTO.TransaccionDTO tx = new FinanzasDashboardResponseDTO.TransaccionDTO();
            tx.setFechaHora(fechaVenta);
            tx.setTipo("INGRESO");
            tx.setOrigen("VENTA");
            tx.setTipoComprobante(v.getTipoDocumento() != null ? v.getTipoDocumento() : "OTRO");
            tx.setComprobante(v.getNumeroDocumento() != null ? v.getNumeroDocumento() : "S/D");
            tx.setEntidad(v.getNombreCliente() != null ? v.getNombreCliente() : "Cliente Libre");
            tx.setMoneda(v.getMoneda());
            tx.setMontoTotal(orZero(v.getTotal())); // Mostramos en tabla la moneda original

            transacciones.add(tx);

            // Sumamos a KPIs
            totalIngresos = totalIngresos.add(montoNormalizado);
            igvVentas = igvVentas.add(igvNormalizado);
            retenciones = retenciones.add(retencionNormalizada);
            detracciones = detracciones.add(detraccionNormalizada);
        }

        // ==========================================
        // 3. PROCESAR CAJA CHICA (INGRESOS / EGRESOS)
        // ==========================================
        List<MovimientoCaja> movimientos = cajaRepository.findAll();

        for (MovimientoCaja m : movimientos) {
            if (m.getFechaHora().isBefore(inicio) || m.getFechaHora().isAfter(fin)) continue;

            FinanzasDashboardResponseDTO.TransaccionDTO tx = new FinanzasDashboardResponseDTO.TransaccionDTO();
            tx.setFechaHora(m.getFechaHora());
            tx.setTipo(m.getTipo()); // "INGRESO" o "EGRESO"
            tx.setOrigen("CAJA_CHICA");
            tx.setTipoComprobante("TICKET/RECIBO");
            tx.setComprobante(m.getMotivo());
            tx.setEntidad(m.getResponsable());
            tx.setMoneda("PEN"); // La caja chica la manejaremos como soles por defecto
            tx.setMontoTotal(orZero(m.getMonto()));

            transacciones.add(tx);

            // Caja chica siempre suma al efectivo
            if ("INGRESO".equalsIgnoreCase(m.getTipo())) {
                totalIngresos = totalIngresos.add(orZero(m.getMonto()));
            } else {
                totalEgresos = totalEgresos.add(orZero(m.getMonto()));
            }
        }

        // ==========================================
        // 4. ORDENAR CRONOLÓGICAMENTE Y ARMAR RESPUESTA
        // ==========================================
        // Ordenamos la lista: Las fechas más recientes primero
        transacciones.sort(Comparator.comparing(FinanzasDashboardResponseDTO.TransaccionDTO::getFechaHora).reversed());

        dashboard.setTransacciones(transacciones);

        // Guardamos KPIs con redondeo a 2 decimales para que el Frontend no explote
        dashboard.setTotalIngresosEfectivos(totalIngresos.setScale(2, RoundingMode.HALF_UP));
        dashboard.setTotalEgresosEfectivos(totalEgresos.setScale(2, RoundingMode.HALF_UP));
        dashboard.setBalanceNeto(totalIngresos.subtract(totalEgresos).setScale(2, RoundingMode.HALF_UP));

        dashboard.setTotalIgvPercibido(igvVentas.setScale(2, RoundingMode.HALF_UP));
        dashboard.setTotalIgvPagado(igvCompras.setScale(2, RoundingMode.HALF_UP));
        dashboard.setBalanceIgv(igvVentas.subtract(igvCompras).setScale(2, RoundingMode.HALF_UP));

        dashboard.setTotalRetenciones(retenciones.setScale(2, RoundingMode.HALF_UP));
        dashboard.setTotalDetracciones(detracciones.setScale(2, RoundingMode.HALF_UP));
        dashboard.setTotalPercepciones(percepciones.setScale(2, RoundingMode.HALF_UP));

        return dashboard;
    }

    // Helper para evitar problemas con nulos
    private BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}