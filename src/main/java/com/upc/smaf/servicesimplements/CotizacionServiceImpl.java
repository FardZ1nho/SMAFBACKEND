package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.CotizacionRequestDTO;
import com.upc.smaf.entities.*;
import com.upc.smaf.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CotizacionServiceImpl {

    private final CotizacionRepository cotizacionRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final PdfService pdfService; // ✅ Inyectamos tu servicio de PDF corregido

    @Transactional
    public Cotizacion registrar(CotizacionRequestDTO dto) {
        // 1. Validar Cliente
        Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + dto.getIdCliente()));

        // 2. Crear Cabecera
        Cotizacion cot = new Cotizacion();
        cot.setCliente(cliente);
        cot.setFechaEmision(LocalDateTime.now());
        cot.setFechaVencimiento(dto.getFechaVencimiento());
        cot.setMoneda(dto.getMoneda());
        cot.setTipoCambio(dto.getTipoCambio());
        cot.setObservaciones(dto.getObservaciones());
        cot.setEstado(Cotizacion.EstadoCotizacion.ENVIADA);

        // 3. Generar Serie/Número (Lógica simple: COT + correlativo)
        // Nota: En producción esto debería ser más robusto para evitar duplicados concurrentes
        long cantidad = cotizacionRepository.count() + 1;
        cot.setSerie("COT");
        cot.setNumero(String.format("%06d", cantidad)); // Ej: 000001

        // 4. Asignar Totales
        cot.setSubTotal(dto.getSubTotal());
        cot.setIgv(dto.getIgv());
        cot.setTotal(dto.getTotal());

        // 5. Procesar Detalles
        List<CotizacionDetalle> detalles = new ArrayList<>();
        if (dto.getDetalles() != null) {
            for (CotizacionRequestDTO.DetalleCotizacionDTO d : dto.getDetalles()) {
                Producto prod = productoRepository.findById(d.getIdProducto())
                        .orElseThrow(() -> new RuntimeException("Producto no existe ID: " + d.getIdProducto()));

                CotizacionDetalle det = new CotizacionDetalle();
                det.setCotizacion(cot);
                det.setProducto(prod);
                det.setCantidad(d.getCantidad());
                det.setPrecioUnitario(d.getPrecioUnitario());

                // Calcular importe por seguridad (Cantidad * Precio)
                BigDecimal importe = d.getPrecioUnitario().multiply(new BigDecimal(d.getCantidad()));
                det.setImporte(importe);

                detalles.add(det);
            }
        }
        cot.setDetalles(detalles);

        return cotizacionRepository.save(cot);
    }

    // ✅ Método para obtener los bytes del PDF
    public byte[] obtenerPdf(Integer id) {
        Cotizacion cot = cotizacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cotización no encontrada"));

        // Llamamos al servicio que arreglamos en el paso anterior
        return pdfService.generarCotizacionPDF(cot);
    }

    // ✅ Método para Opción B: Aprobar y convertir (Lógica básica de cambio de estado)
    @Transactional
    public void aprobarCotizacion(Integer id) {
        Cotizacion cot = cotizacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe la cotización"));

        if (cot.getEstado() == Cotizacion.EstadoCotizacion.APROBADA) {
            return; // Ya estaba aprobada
        }

        cot.setEstado(Cotizacion.EstadoCotizacion.APROBADA);
        cotizacionRepository.save(cot);

        // AQUÍ FUTURO: Llamar a VentaService.crearDesdeCotizacion(cot);
    }

    public List<Cotizacion> listarTodas() {
        return cotizacionRepository.findAll();
    }
}