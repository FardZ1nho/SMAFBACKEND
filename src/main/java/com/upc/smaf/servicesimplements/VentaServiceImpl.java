package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.DetalleVentaRequestDTO;
import com.upc.smaf.dtos.request.VentaRequestDTO;
import com.upc.smaf.dtos.response.DetalleVentaResponseDTO;
import com.upc.smaf.dtos.response.VentaResponseDTO;
import com.upc.smaf.entities.*;
import com.upc.smaf.entities.EstadoVenta;
import com.upc.smaf.entities.MetodoPago;
import com.upc.smaf.repositories.ProductoRepository;
import com.upc.smaf.repositories.VentaRepository;
import com.upc.smaf.serviceinterface.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;

    private static final BigDecimal IGV_PORCENTAJE = new BigDecimal("0.18"); // 18%

    // ========== CREAR VENTA COMPLETA ==========

    @Override
    @Transactional
    public VentaResponseDTO crearVenta(VentaRequestDTO request) {
        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            throw new RuntimeException("Debe agregar al menos un producto a la venta");
        }

        Venta venta = new Venta();
        venta.setCodigo(generarCodigoVenta());
        venta.setFechaVenta(request.getFechaVenta() != null ? request.getFechaVenta() : LocalDateTime.now());

        // Mapear datos básicos
        venta.setNombreCliente(request.getNombreCliente());
        venta.setTipoCliente(request.getTipoCliente());
        venta.setNotas(request.getNotas());
        venta.setEstado(EstadoVenta.COMPLETADA);

        // Mapear Pagos, Moneda y Documentos (Factura, Boleta, Número)
        mapearDatosPagoYDocumento(venta, request);

        // Procesar productos (Descontando stock porque es venta completa)
        for (DetalleVentaRequestDTO detalleDTO : request.getDetalles()) {
            procesarDetalleVenta(venta, detalleDTO, true);
        }

        calcularTotales(venta);
        Venta ventaGuardada = ventaRepository.save(venta);
        return convertirAResponseDTO(ventaGuardada);
    }

    // ========== GUARDAR BORRADOR ==========

    @Override
    @Transactional
    public VentaResponseDTO guardarBorrador(VentaRequestDTO request) {
        Venta venta = new Venta();
        venta.setCodigo(generarCodigoVenta());
        venta.setFechaVenta(request.getFechaVenta() != null ? request.getFechaVenta() : LocalDateTime.now());

        // Mapear datos básicos
        venta.setNombreCliente(request.getNombreCliente());
        venta.setTipoCliente(request.getTipoCliente());
        venta.setNotas(request.getNotas());
        venta.setEstado(EstadoVenta.BORRADOR);

        // Mapear Pagos, Moneda y Documentos
        mapearDatosPagoYDocumento(venta, request);

        // Agregar detalles SIN descontar stock
        if (request.getDetalles() != null && !request.getDetalles().isEmpty()) {
            for (DetalleVentaRequestDTO detalleDTO : request.getDetalles()) {
                procesarDetalleVenta(venta, detalleDTO, false);
            }
        }

        calcularTotales(venta);
        Venta ventaGuardada = ventaRepository.save(venta);
        return convertirAResponseDTO(ventaGuardada);
    }

    // ========== CONVERTIR BORRADOR A VENTA (COMPLETAR) ==========

    @Override
    @Transactional
    public VentaResponseDTO completarVenta(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));

        if (venta.getEstado() != EstadoVenta.BORRADOR) {
            throw new RuntimeException("Solo se pueden completar ventas en estado BORRADOR");
        }

        // Validar y Descontar stock ahora que se completa
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();

            // Validación estricta de stock al completar
            if (producto.getStockActual() < detalle.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre() +
                        ". Disponible: " + producto.getStockActual() +
                        ", Solicitado: " + detalle.getCantidad());
            }

            producto.setStockActual(producto.getStockActual() - detalle.getCantidad());
            productoRepository.save(producto);
        }

        venta.setEstado(EstadoVenta.COMPLETADA);
        Venta ventaActualizada = ventaRepository.save(venta);
        return convertirAResponseDTO(ventaActualizada);
    }

    // ========== ACTUALIZAR VENTA (SOLO BORRADORES) ==========

    @Override
    @Transactional
    public VentaResponseDTO actualizarVenta(Integer id, VentaRequestDTO request) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));

        if (venta.getEstado() == EstadoVenta.COMPLETADA) {
            throw new RuntimeException("No se puede actualizar una venta COMPLETADA");
        }

        // Actualizar datos básicos
        venta.setNombreCliente(request.getNombreCliente());
        venta.setTipoCliente(request.getTipoCliente());
        venta.setNotas(request.getNotas());

        // Actualizar fecha si viene en el request
        if(request.getFechaVenta() != null) {
            venta.setFechaVenta(request.getFechaVenta());
        }

        // Actualizar Pagos, Moneda y Documentos
        mapearDatosPagoYDocumento(venta, request);

        // Limpiar detalles antiguos y rehacer
        venta.getDetalles().clear();
        if (request.getDetalles() != null) {
            for (DetalleVentaRequestDTO detalleDTO : request.getDetalles()) {
                procesarDetalleVenta(venta, detalleDTO, false);
            }
        }

        calcularTotales(venta);
        Venta ventaActualizada = ventaRepository.save(venta);
        return convertirAResponseDTO(ventaActualizada);
    }

    // ========== MÉTODOS DE LECTURA Y UTILITARIOS ==========

    @Override
    @Transactional
    public void cancelarVenta(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));

        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            throw new RuntimeException("La venta ya está cancelada");
        }

        // Si estaba completada, devolvemos el stock
        if (venta.getEstado() == EstadoVenta.COMPLETADA) {
            for (DetalleVenta detalle : venta.getDetalles()) {
                Producto producto = detalle.getProducto();
                producto.setStockActual(producto.getStockActual() + detalle.getCantidad());
                productoRepository.save(producto);
            }
        }

        venta.setEstado(EstadoVenta.CANCELADA);
        ventaRepository.save(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO obtenerVenta(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
        return convertirAResponseDTO(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO buscarPorCodigo(String codigo) {
        Venta venta = ventaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con código: " + codigo));
        return convertirAResponseDTO(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listarTodas() {
        return ventaRepository.findAll().stream().map(this::convertirAResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listarPorEstado(EstadoVenta estado) {
        return ventaRepository.findByEstado(estado).stream().map(this::convertirAResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listarBorradores() { return listarPorEstado(EstadoVenta.BORRADOR); }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listarCompletadas() { return listarPorEstado(EstadoVenta.COMPLETADA); }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listarPorFecha(LocalDateTime inicio, LocalDateTime fin) {
        return ventaRepository.findByFechaVentaBetween(inicio, fin).stream().map(this::convertirAResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> buscarPorCliente(String nombreCliente) {
        return ventaRepository.buscarPorCliente(nombreCliente).stream().map(this::convertirAResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminarVenta(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
        if (venta.getEstado() == EstadoVenta.COMPLETADA) {
            throw new RuntimeException("No se puede eliminar una venta COMPLETADA.");
        }
        ventaRepository.delete(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarVentasPorEstado(EstadoVenta estado) { return ventaRepository.contarPorEstado(estado); }

    @Override
    @Transactional
    public VentaResponseDTO convertirBorradorAVenta(Integer id) { return completarVenta(id); }

    // ========== MÉTODOS AUXILIARES (LÓGICA INTERNA) ==========

    /**
     * Centraliza la lógica de guardar moneda, pago mixto y los documentos.
     */
    private void mapearDatosPagoYDocumento(Venta venta, VentaRequestDTO request) {
        // 1. Mapear Moneda y TC
        venta.setMoneda(request.getMoneda());
        venta.setTipoCambio(request.getTipoCambio());
        venta.setMetodoPago(request.getMetodoPago());

        // 2. Mapear Documento (Factura/Boleta y Número)
        venta.setTipoDocumento(request.getTipoDocumento());
        venta.setNumeroDocumento(request.getNumeroDocumento());

        // 3. Lógica de Pago Mixto
        if (MetodoPago.MIXTO.equals(request.getMetodoPago())) {
            // Si es mixto, guardamos los valores que vienen del front
            venta.setPagoEfectivo(request.getPagoEfectivo() != null ? request.getPagoEfectivo() : BigDecimal.ZERO);
            venta.setPagoTransferencia(request.getPagoTransferencia() != null ? request.getPagoTransferencia() : BigDecimal.ZERO);
        } else {
            // Si NO es mixto, limpiamos estos campos para mantener la BD ordenada
            venta.setPagoEfectivo(BigDecimal.ZERO);
            venta.setPagoTransferencia(BigDecimal.ZERO);
        }
    }

    private void procesarDetalleVenta(Venta venta, DetalleVentaRequestDTO detalleDTO, boolean descontarStock) {
        Producto producto = productoRepository.findById(detalleDTO.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + detalleDTO.getProductoId()));

        if (descontarStock && producto.getStockActual() < detalleDTO.getCantidad()) {
            throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
        }

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(detalleDTO.getCantidad());
        detalle.setPrecioUnitario(detalleDTO.getPrecioUnitario());
        detalle.setDescuento(detalleDTO.getDescuento() != null ? detalleDTO.getDescuento() : BigDecimal.ZERO);
        detalle.calcularSubtotal();

        venta.agregarDetalle(detalle);

        if (descontarStock) {
            producto.setStockActual(producto.getStockActual() - detalleDTO.getCantidad());
            productoRepository.save(producto);
        }
    }

    private void calcularTotales(Venta venta) {
        BigDecimal subtotal = venta.getDetalles().stream()
                .map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal igv = subtotal.multiply(IGV_PORCENTAJE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv);

        venta.setSubtotal(subtotal);
        venta.setIgv(igv);
        venta.setTotal(total);
    }

    private String generarCodigoVenta() {
        String anio = String.valueOf(LocalDateTime.now().getYear());
        Long contador = ventaRepository.count() + 1;
        return String.format("VTA-%s-%04d", anio, contador);
    }

    private VentaResponseDTO convertirAResponseDTO(Venta venta) {
        VentaResponseDTO dto = new VentaResponseDTO();
        dto.setId(venta.getId());
        dto.setCodigo(venta.getCodigo());
        dto.setFechaVenta(venta.getFechaVenta());
        dto.setNombreCliente(venta.getNombreCliente());
        dto.setTipoCliente(venta.getTipoCliente());

        // Campos de Pago y Moneda
        dto.setMetodoPago(venta.getMetodoPago());
        dto.setPagoEfectivo(venta.getPagoEfectivo());
        dto.setPagoTransferencia(venta.getPagoTransferencia());
        dto.setMoneda(venta.getMoneda());
        dto.setTipoCambio(venta.getTipoCambio());

        // Campos de Documento
        dto.setTipoDocumento(venta.getTipoDocumento());
        dto.setNumeroDocumento(venta.getNumeroDocumento());

        dto.setSubtotal(venta.getSubtotal());
        dto.setIgv(venta.getIgv());
        dto.setTotal(venta.getTotal());
        dto.setNotas(venta.getNotas());
        dto.setEstado(venta.getEstado());
        dto.setFechaCreacion(venta.getFechaCreacion());

        List<DetalleVentaResponseDTO> detallesDTO = venta.getDetalles().stream()
                .map(this::convertirDetalleAResponseDTO)
                .collect(Collectors.toList());
        dto.setDetalles(detallesDTO);

        return dto;
    }

    private DetalleVentaResponseDTO convertirDetalleAResponseDTO(DetalleVenta detalle) {
        DetalleVentaResponseDTO dto = new DetalleVentaResponseDTO();
        dto.setId(detalle.getId());
        dto.setProductoId(detalle.getProducto().getId());
        dto.setProductoNombre(detalle.getProducto().getNombre());
        dto.setProductoCodigo(detalle.getProducto().getCodigo());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setDescuento(detalle.getDescuento());
        dto.setSubtotal(detalle.getSubtotal());
        return dto;
    }
}