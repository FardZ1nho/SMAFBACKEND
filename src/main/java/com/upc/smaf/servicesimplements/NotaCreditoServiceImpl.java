package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.NotaCreditoRequestDTO;
import com.upc.smaf.dtos.response.NotaCreditoResponseDTO;
import com.upc.smaf.entities.*;
import com.upc.smaf.repositories.NotaCreditoRepository;
import com.upc.smaf.repositories.ProductoRepository;
import com.upc.smaf.repositories.VentaRepository;
import com.upc.smaf.serviceinterface.NotaCreditoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotaCreditoServiceImpl implements NotaCreditoService {

    private final NotaCreditoRepository notaCreditoRepository;
    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional
    public NotaCreditoResponseDTO emitirNotaCredito(NotaCreditoRequestDTO request) {
        // 1. Buscar la venta original
        Venta venta = ventaRepository.findById(request.getVentaId())
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        // 2. Validar que la venta esté completada
        if (venta.getEstado() != EstadoVenta.COMPLETADA) {
            throw new RuntimeException("Solo se pueden emitir notas de crédito a ventas COMPLETADAS");
        }

        // 3. Validar monto (No puedes devolver más de lo que cobraste)
        if (request.getMonto().compareTo(venta.getTotal()) > 0) {
            throw new RuntimeException("El monto de la Nota de Crédito no puede superar el total de la venta");
        }

        // 4. Crear la Entidad
        NotaCredito nc = new NotaCredito();
        nc.setVentaOriginal(venta);
        nc.setMotivo(request.getMotivo());
        nc.setMontoTotal(request.getMonto());
        nc.setMoneda(venta.getMoneda());
        nc.setObservaciones(request.getObservaciones());

        // Generar Serie y Número (Lógica simple)
        nc.setSerie("NC01");
        nc.setNumero(generarNumeroCorrelativo());

        // 5. LÓGICA DE STOCK (Crucial)
        // Si el motivo implica devolución de mercadería, regresamos el stock.
        if (esMotivoDeDevolucion(request.getMotivo())) {
            devolverStockAlInventario(venta);
        }

        // 6. Guardar y Retornar
        NotaCredito ncGuardada = notaCreditoRepository.save(nc);
        return convertirADTO(ncGuardada);
    }

    // --- Métodos Auxiliares ---

    private boolean esMotivoDeDevolucion(MotivoNota motivo) {
        return motivo == MotivoNota.DEVOLUCION_TOTAL ||
                motivo == MotivoNota.ANULACION_DE_LA_OPERACION ||
                motivo == MotivoNota.DEVOLUCION_POR_ITEM;
    }

    private void devolverStockAlInventario(Venta venta) {
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            // Sumamos lo que se vendió de vuelta al stock actual
            producto.setStockActual(producto.getStockActual() + detalle.getCantidad());
            productoRepository.save(producto);
        }
    }

    private String generarNumeroCorrelativo() {
        long cantidad = notaCreditoRepository.count() + 1;
        return String.format("%08d", cantidad); // Ej: 00000001
    }

    private NotaCreditoResponseDTO convertirADTO(NotaCredito nc) {
        NotaCreditoResponseDTO dto = new NotaCreditoResponseDTO();
        dto.setId(nc.getId());
        dto.setCodigoCompleto(nc.getSerie() + "-" + nc.getNumero());
        dto.setCodigoVentaAfectada(nc.getVentaOriginal().getCodigo());
        dto.setMotivo(nc.getMotivo());
        dto.setMontoTotal(nc.getMontoTotal());
        dto.setMoneda(nc.getMoneda());
        dto.setFechaEmision(nc.getFechaEmision());
        dto.setObservaciones(nc.getObservaciones());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotaCreditoResponseDTO> listarTodas() {
        return notaCreditoRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotaCreditoResponseDTO> listarPorVenta(Integer ventaId) {
        return notaCreditoRepository.findByVentaOriginalId(ventaId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal obtenerTotalDevoluciones() {
        return notaCreditoRepository.sumarTotalDevoluciones();
    }
}