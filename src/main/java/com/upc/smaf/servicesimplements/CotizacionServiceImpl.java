package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.CotizacionRequestDTO;
import com.upc.smaf.entities.*;
import com.upc.smaf.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CotizacionServiceImpl {

    private final CotizacionRepository cotizacionRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final PdfService pdfService;

    // ==========================================
    // 1. CREAR COTIZACIÓN Y CALCULAR RENTABILIDAD
    // ==========================================
    @Transactional
    public Cotizacion registrar(CotizacionRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + dto.getIdCliente()));

        Cotizacion cot = new Cotizacion();
        cot.setCliente(cliente);
        cot.setFechaEmision(LocalDateTime.now());
        cot.setFechaVencimiento(dto.getFechaVencimiento());
        cot.setMoneda(dto.getMoneda());

        // Si no mandan tipo de cambio, evitamos errores matemáticos
        BigDecimal tipoCambio = (dto.getTipoCambio() != null && dto.getTipoCambio().compareTo(BigDecimal.ZERO) > 0)
                ? dto.getTipoCambio() : new BigDecimal("3.80");
        cot.setTipoCambio(tipoCambio);

        cot.setObservaciones(dto.getObservaciones());

        // CRM: Al crearla, entra directamente a este estado del embudo
        cot.setEstado(Cotizacion.EstadoCotizacion.COTIZACION_ENVIADA);

        // Generar Serie/Número
        long cantidad = cotizacionRepository.count() + 1;
        cot.setSerie("COT");
        cot.setNumero(String.format("%06d", cantidad));

        cot.setSubTotal(dto.getSubTotal());
        cot.setIgv(dto.getIgv());
        cot.setTotal(dto.getTotal());

        // --- PROCESAR DETALLES Y CALCULAR GANANCIA ---
        List<CotizacionDetalle> detalles = new ArrayList<>();
        BigDecimal costoTotalAcumulado = BigDecimal.ZERO;

        if (dto.getDetalles() != null) {
            for (CotizacionRequestDTO.DetalleCotizacionDTO d : dto.getDetalles()) {
                Producto prod = productoRepository.findById(d.getIdProducto())
                        .orElseThrow(() -> new RuntimeException("Producto no existe ID: " + d.getIdProducto()));

                CotizacionDetalle det = new CotizacionDetalle();
                det.setCotizacion(cot);
                det.setProducto(prod);
                det.setCantidad(d.getCantidad());
                det.setPrecioUnitario(d.getPrecioUnitario());

                BigDecimal importe = d.getPrecioUnitario().multiply(new BigDecimal(d.getCantidad()));
                det.setImporte(importe);
                detalles.add(det);

                // ⭐ CRM: Simulador de Rentabilidad Secreta
                BigDecimal costoProducto = prod.getCostoTotal() != null ? prod.getCostoTotal() : BigDecimal.ZERO;
                String monedaProducto = prod.getMoneda() != null ? prod.getMoneda() : "PEN";

                // Normalizamos el costo a la moneda de la cotización
                BigDecimal costoConvertido = normalizarMonto(costoProducto, monedaProducto, cot.getMoneda(), tipoCambio);
                costoTotalAcumulado = costoTotalAcumulado.add(costoConvertido.multiply(new BigDecimal(d.getCantidad())));
            }
        }
        cot.setDetalles(detalles);

        // ⭐ CRM: Guardamos el margen de ganancia estimado (Subtotal de venta - Costo de productos)
        // Usamos el Subtotal (sin IGV) para que el cálculo de ganancia sea real, ya que el IGV es del Estado.
        BigDecimal margenEstimado = cot.getSubTotal().subtract(costoTotalAcumulado);
        cot.setMargenGananciaEstimado(margenEstimado);

        return cotizacionRepository.save(cot);
    }

    // ==========================================
    // 2. GESTIÓN DEL PIPELINE (EMBUDO CRM)
    // ==========================================
    @Transactional
    public Cotizacion actualizarEstadoPipeline(Integer id, Cotizacion.EstadoCotizacion nuevoEstado, String motivoPerdida) {
        Cotizacion cot = cotizacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cotización no encontrada"));

        cot.setEstado(nuevoEstado);

        // Si la venta se perdió, guardamos el por qué (Inteligencia de Negocio)
        if (nuevoEstado == Cotizacion.EstadoCotizacion.PERDIDA) {
            cot.setMotivoPerdida(motivoPerdida);
        } else {
            cot.setMotivoPerdida(null); // Limpiamos si se equivocó y la vuelve a abrir
        }

        // Si la venta se gana, en el futuro aquí llamaremos a la VentaService para crear el ticket y restar stock
        if (nuevoEstado == Cotizacion.EstadoCotizacion.GANADA) {
            // TODO: Integrar con VentaService.crearDesdeCotizacion(cot);
            System.out.println("¡Venta Ganada! Lista para facturar.");
        }

        return cotizacionRepository.save(cot);
    }

    // ==========================================
    // 3. GENERACIÓN DE PDF
    // ==========================================
    public byte[] obtenerPdf(Integer id) {
        Cotizacion cot = cotizacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cotización no encontrada"));

        return pdfService.generarCotizacionPDF(cot);
    }

    // ==========================================
    // 4. CONSULTAS
    // ==========================================
    public List<Cotizacion> listarTodas() {
        return cotizacionRepository.findAll();
    }

    public Cotizacion obtenerPorId(Integer id) {
        return cotizacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cotización no encontrada con ID: " + id));
    }

    // ==========================================
    // UTILIDADES
    // ==========================================
    private BigDecimal normalizarMonto(BigDecimal monto, String monedaOrigen, String monedaDestino, BigDecimal tipoCambio) {
        if (monedaOrigen.equals(monedaDestino)) {
            return monto;
        }
        if ("USD".equals(monedaOrigen) && "PEN".equals(monedaDestino)) {
            return monto.multiply(tipoCambio).setScale(2, RoundingMode.HALF_UP);
        }
        if ("PEN".equals(monedaOrigen) && "USD".equals(monedaDestino)) {
            if (tipoCambio.compareTo(BigDecimal.ZERO) == 0) return monto;
            return monto.divide(tipoCambio, 2, RoundingMode.HALF_UP);
        }
        return monto;
    }
}