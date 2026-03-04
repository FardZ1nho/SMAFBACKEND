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

            // ✅ CORRECCIÓN: Usar estrictamente la Fecha de Emisión de la Factura
            LocalDateTime fechaCompra = c.getFechaEmision() != null ? c.getFechaEmision().atStartOfDay() : (c.getFechaRegistro() != null ? c.getFechaRegistro() : LocalDateTime.now());
            if (fechaCompra.isBefore(inicio) || fechaCompra.isAfter(fin)) continue;

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

            if (c.getProveedor() != null) {
                tx.setEntidad(c.getProveedor().getNombre());
                tx.setRuc(c.getProveedor().getRuc());
            } else {
                tx.setEntidad(c.getNombreProveedor() != null ? c.getNombreProveedor() : "Proveedor Libre");
                tx.setRuc(c.getRucProveedor() != null ? c.getRucProveedor() : "S/D");
            }

            tx.setDescripcion(c.getObservaciones() != null && !c.getObservaciones().trim().isEmpty() ? c.getObservaciones() : "COMPRA DE MERCADERÍA / SERVICIOS");
            tx.setMoneda(c.getMoneda());
            tx.setMontoTotal(orZero(c.getTotal()));
            tx.setSubTotal(orZero(c.getSubTotal()));
            tx.setIgv(orZero(c.getIgv()));
            tx.setTipoCambio(tipoCambio);

            // ✅ AGREGANDO LOS NUEVOS CAMPOS
            tx.setRetencion(orZero(c.getRetencion()));
            tx.setDetraccion(orZero(c.getDetraccionMonto()));
            tx.setPercepcion(orZero(c.getPercepcion()));

            transacciones.add(tx);

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
            if (v.getEstado() != null && v.getEstado().name().equals("ANULADA")) continue;

            // ✅ CORRECCIÓN: Usar la fecha registrada en la Venta (asumiendo getFechaVenta como la fecha del documento)
            LocalDateTime fechaVenta = v.getFechaVenta() != null ? v.getFechaVenta() : LocalDateTime.now();
            if (fechaVenta.isBefore(inicio) || fechaVenta.isAfter(fin)) continue;

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
            tx.setRuc(v.getNumeroDocumento() != null ? v.getNumeroDocumento() : "S/D");

            tx.setDescripcion("VENTA DE PRODUCTOS / SERVICIOS");
            tx.setMoneda(v.getMoneda());
            tx.setMontoTotal(orZero(v.getTotal()));
            tx.setSubTotal(orZero(v.getTotal()).subtract(orZero(v.getIgv())));
            tx.setIgv(orZero(v.getIgv()));
            tx.setTipoCambio(tipoCambio);

            // ✅ AGREGANDO LOS NUEVOS CAMPOS (Venta no tiene Percepción en tu modelo actual, va en 0)
            tx.setRetencion(orZero(v.getRetencion()));
            tx.setDetraccion(orZero(v.getDetraccion()));
            tx.setPercepcion(BigDecimal.ZERO);

            transacciones.add(tx);

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
            tx.setTipo(m.getTipo());
            tx.setOrigen("CAJA_CHICA");
            tx.setTipoComprobante("TICKET/RECIBO");
            tx.setComprobante("S/D");
            tx.setEntidad(m.getResponsable());

            tx.setRuc("S/D");
            tx.setDescripcion(m.getMotivo());
            tx.setMoneda("PEN");
            tx.setMontoTotal(orZero(m.getMonto()));
            tx.setSubTotal(orZero(m.getMonto()));
            tx.setIgv(BigDecimal.ZERO);
            tx.setTipoCambio(BigDecimal.ONE);

            // ✅ AGREGANDO LOS NUEVOS CAMPOS
            tx.setRetencion(BigDecimal.ZERO);
            tx.setDetraccion(BigDecimal.ZERO);
            tx.setPercepcion(BigDecimal.ZERO);

            transacciones.add(tx);

            if ("INGRESO".equalsIgnoreCase(m.getTipo())) {
                totalIngresos = totalIngresos.add(orZero(m.getMonto()));
            } else {
                totalEgresos = totalEgresos.add(orZero(m.getMonto()));
            }
        }

        // ==========================================
        // 4. ORDENAR Y ARMAR RESPUESTA
        // ==========================================
        transacciones.sort(Comparator.comparing(FinanzasDashboardResponseDTO.TransaccionDTO::getFechaHora).reversed());

        dashboard.setTransacciones(transacciones);
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

    private BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}