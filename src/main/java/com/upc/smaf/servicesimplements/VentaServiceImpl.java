package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.DetalleVentaRequestDTO;
import com.upc.smaf.dtos.request.VentaRequestDTO;
import com.upc.smaf.dtos.response.DetalleVentaResponseDTO;
import com.upc.smaf.dtos.response.VentaResponseDTO;
import com.upc.smaf.entities.*;
import com.upc.smaf.repositories.CuentaBancariaRepository;
import com.upc.smaf.repositories.ProductoRepository;
import com.upc.smaf.repositories.VentaRepository;
import com.upc.smaf.serviceinterface.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final CuentaBancariaRepository cuentaRepository;

    private static final BigDecimal IGV_PORCENTAJE = new BigDecimal("0.18");

    // ==========================================
    // 1. CREAR VENTA
    // ==========================================
    @Override
    @Transactional
    public VentaResponseDTO crearVenta(VentaRequestDTO request) {
        // Validaciones iniciales
        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            throw new RuntimeException("Debe agregar al menos un producto a la venta");
        }

        Venta venta = new Venta();
        venta.setCodigo(generarCodigoVenta());
        venta.setFechaVenta(request.getFechaVenta() != null ? request.getFechaVenta() : LocalDateTime.now());
        venta.setNombreCliente(request.getNombreCliente());
        venta.setTipoCliente(request.getTipoCliente());
        venta.setNotas(request.getNotas());

        venta.setMoneda(request.getMoneda());
        venta.setTipoCambio(request.getTipoCambio());
        venta.setTipoPago(request.getTipoPago());
        venta.setNumeroCuotas(request.getNumeroCuotas());

        venta.setTipoDocumento(request.getTipoDocumento());
        venta.setNumeroDocumento(request.getNumeroDocumento());

        // --- PROCESAR PRODUCTOS ---
        BigDecimal subtotalAcumulado = BigDecimal.ZERO;

        for (DetalleVentaRequestDTO detalleDTO : request.getDetalles()) {
            Producto producto = productoRepository.findById(detalleDTO.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado ID: " + detalleDTO.getProductoId()));

            if (producto.getStockActual() < detalleDTO.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(detalleDTO.getCantidad());
            detalle.setPrecioUnitario(detalleDTO.getPrecioUnitario());
            detalle.setDescuento(detalleDTO.getDescuento() != null ? detalleDTO.getDescuento() : BigDecimal.ZERO);
            detalle.calcularSubtotal();

            subtotalAcumulado = subtotalAcumulado.add(detalle.getSubtotal());
            venta.agregarDetalle(detalle);

            // Actualizar stock
            producto.setStockActual(producto.getStockActual() - detalleDTO.getCantidad());
            productoRepository.save(producto);
        }

        // --- TOTALES ---
        BigDecimal totalVenta = subtotalAcumulado;
        BigDecimal subtotalBase = totalVenta.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP);
        BigDecimal igv = totalVenta.subtract(subtotalBase);

        venta.setSubtotal(subtotalBase);
        venta.setIgv(igv);
        venta.setTotal(totalVenta);

        // --- PROCESAR PAGOS ---
        BigDecimal totalPagadoNormalizado = BigDecimal.ZERO;

        if (request.getPagos() != null && !request.getPagos().isEmpty()) {
            for (VentaRequestDTO.PagoRequestDTO pagoDTO : request.getPagos()) {
                Pago pago = new Pago();
                pago.setMetodoPago(pagoDTO.getMetodoPago());
                pago.setMonto(pagoDTO.getMonto());
                pago.setMoneda(pagoDTO.getMoneda());
                pago.setFechaPago(LocalDateTime.now());
                pago.setReferencia(pagoDTO.getReferencia());

                if (pagoDTO.getCuentaBancariaId() != null) {
                    cuentaRepository.findById(pagoDTO.getCuentaBancariaId())
                            .ifPresent(pago::setCuentaDestino);
                }

                BigDecimal montoEnMonedaVenta = normalizarMonto(
                        pagoDTO.getMonto(), pagoDTO.getMoneda(),
                        venta.getMoneda(), venta.getTipoCambio()
                );
                totalPagadoNormalizado = totalPagadoNormalizado.add(montoEnMonedaVenta);

                // ✅ IMPORTANTE: Usamos el método helper para vincular (Cascade)
                venta.agregarPago(pago);
            }
        }

        // --- ESTADO Y SALDO ---
        venta.setMontoInicial(totalPagadoNormalizado);

        if (venta.getTipoPago() == TipoPago.CONTADO) {
            BigDecimal diferencia = totalVenta.subtract(totalPagadoNormalizado);
            if (diferencia.compareTo(new BigDecimal("0.10")) > 0) {
                throw new RuntimeException("Pago incompleto para venta al CONTADO.");
            }
            venta.setSaldoPendiente(BigDecimal.ZERO);
            venta.setEstado(EstadoVenta.COMPLETADA);
        } else {
            BigDecimal saldo = totalVenta.subtract(totalPagadoNormalizado);
            if (saldo.compareTo(BigDecimal.ZERO) < 0) saldo = BigDecimal.ZERO;
            venta.setSaldoPendiente(saldo);

            if (saldo.compareTo(BigDecimal.ZERO) > 0) {
                venta.setEstado(EstadoVenta.PENDIENTE);
                if (venta.getNumeroCuotas() != null && venta.getNumeroCuotas() > 0) {
                    venta.setMontoCuota(saldo.divide(new BigDecimal(venta.getNumeroCuotas()), 2, RoundingMode.HALF_UP));
                }
            } else {
                venta.setEstado(EstadoVenta.COMPLETADA);
                venta.setMontoCuota(BigDecimal.ZERO);
            }
        }

        Venta ventaGuardada = ventaRepository.save(venta);
        return convertirAResponseDTO(ventaGuardada);
    }

    // ==========================================
    // 2. REGISTRAR AMORTIZACIÓN
    // ==========================================
    @Override
    @Transactional
    public VentaResponseDTO registrarAmortizacion(Integer ventaId, BigDecimal monto, MetodoPago metodo, Integer cuentaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        if (venta.getEstado() == EstadoVenta.CANCELADA) throw new RuntimeException("Venta cancelada");
        if (venta.getSaldoPendiente().compareTo(BigDecimal.ZERO) <= 0) throw new RuntimeException("Venta ya pagada");

        Pago pago = new Pago();
        pago.setMonto(monto);
        pago.setMoneda(venta.getMoneda());
        pago.setMetodoPago(metodo);
        pago.setFechaPago(LocalDateTime.now());
        pago.setReferencia("AMORTIZACIÓN");

        if (cuentaId != null) {
            cuentaRepository.findById(cuentaId).ifPresent(pago::setCuentaDestino);
        }

        venta.agregarPago(pago);

        venta.setSaldoPendiente(venta.getSaldoPendiente().subtract(monto));
        if (venta.getSaldoPendiente().compareTo(BigDecimal.ZERO) <= 0) {
            venta.setEstado(EstadoVenta.COMPLETADA);
        }

        return convertirAResponseDTO(ventaRepository.save(venta));
    }

    // ==========================================
    // 3. GUARDAR BORRADOR
    // ==========================================
    @Override
    @Transactional
    public VentaResponseDTO guardarBorrador(VentaRequestDTO request) {
        Venta venta = new Venta();
        venta.setCodigo(generarCodigoVenta());
        venta.setFechaVenta(request.getFechaVenta() != null ? request.getFechaVenta() : LocalDateTime.now());
        venta.setNombreCliente(request.getNombreCliente());
        venta.setTipoCliente(request.getTipoCliente());
        venta.setEstado(EstadoVenta.BORRADOR);
        venta.setMoneda(request.getMoneda());
        venta.setTipoCambio(request.getTipoCambio());
        venta.setTipoPago(request.getTipoPago());

        // Productos (Sin descontar stock)
        if (request.getDetalles() != null) {
            for (DetalleVentaRequestDTO det : request.getDetalles()) {
                productoRepository.findById(det.getProductoId()).ifPresent(p -> {
                    DetalleVenta detalle = new DetalleVenta();
                    detalle.setProducto(p);
                    detalle.setCantidad(det.getCantidad());
                    detalle.setPrecioUnitario(det.getPrecioUnitario());
                    detalle.setDescuento(det.getDescuento() != null ? det.getDescuento() : BigDecimal.ZERO);
                    detalle.calcularSubtotal();
                    venta.agregarDetalle(detalle);
                });
            }
        }

        BigDecimal total = venta.getDetalles().stream().map(DetalleVenta::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        venta.setTotal(total);
        venta.setSubtotal(total.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP));
        venta.setIgv(total.subtract(venta.getSubtotal()));

        // Pagos en Borrador
        if (request.getPagos() != null) {
            for (VentaRequestDTO.PagoRequestDTO pagoDTO : request.getPagos()) {
                Pago pago = new Pago();
                pago.setMetodoPago(pagoDTO.getMetodoPago());
                pago.setMonto(pagoDTO.getMonto());
                pago.setMoneda(pagoDTO.getMoneda());
                pago.setFechaPago(LocalDateTime.now());
                if (pagoDTO.getCuentaBancariaId() != null) {
                    cuentaRepository.findById(pagoDTO.getCuentaBancariaId()).ifPresent(pago::setCuentaDestino);
                }
                venta.agregarPago(pago);
            }
        }

        return convertirAResponseDTO(ventaRepository.save(venta));
    }

    // ==========================================
    // 4. ACTUALIZAR VENTA
    // ==========================================
    @Override
    @Transactional
    public VentaResponseDTO actualizarVenta(Integer id, VentaRequestDTO request) {
        Venta venta = ventaRepository.findById(id).orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        if (venta.getEstado() == EstadoVenta.COMPLETADA) throw new RuntimeException("No se editan ventas completadas");

        venta.getDetalles().clear();
        venta.getPagos().clear();

        venta.setNombreCliente(request.getNombreCliente());
        venta.setNotas(request.getNotas());

        // Re-agregar productos
        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetalleVentaRequestDTO detDTO : request.getDetalles()) {
            Producto p = productoRepository.findById(detDTO.getProductoId()).orElseThrow();
            DetalleVenta det = new DetalleVenta();
            det.setProducto(p);
            det.setCantidad(detDTO.getCantidad());
            det.setPrecioUnitario(detDTO.getPrecioUnitario());
            det.setDescuento(detDTO.getDescuento() != null ? detDTO.getDescuento() : BigDecimal.ZERO);
            det.calcularSubtotal();
            venta.agregarDetalle(det);
            subtotal = subtotal.add(det.getSubtotal());
        }

        venta.setTotal(subtotal);
        venta.setSubtotal(subtotal.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP));
        venta.setIgv(subtotal.subtract(venta.getSubtotal()));

        // Re-agregar Pagos
        BigDecimal totalPagado = BigDecimal.ZERO;
        if (request.getPagos() != null) {
            for (VentaRequestDTO.PagoRequestDTO pDTO : request.getPagos()) {
                Pago pago = new Pago();
                pago.setMetodoPago(pDTO.getMetodoPago());
                pago.setMonto(pDTO.getMonto());
                pago.setMoneda(pDTO.getMoneda());
                pago.setFechaPago(LocalDateTime.now());
                if (pDTO.getCuentaBancariaId() != null) {
                    cuentaRepository.findById(pDTO.getCuentaBancariaId()).ifPresent(pago::setCuentaDestino);
                }
                totalPagado = totalPagado.add(normalizarMonto(pDTO.getMonto(), pDTO.getMoneda(), venta.getMoneda(), venta.getTipoCambio()));
                venta.agregarPago(pago);
            }
        }
        venta.setMontoInicial(totalPagado);

        return convertirAResponseDTO(ventaRepository.save(venta));
    }

    // ==========================================
    // LECTURA Y OTROS
    // ==========================================
    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO obtenerVenta(Integer id) {
        return ventaRepository.findById(id).map(this::convertirAResponseDTO)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
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

    @Override public List<VentaResponseDTO> listarBorradores() { return listarPorEstado(EstadoVenta.BORRADOR); }
    @Override public List<VentaResponseDTO> listarCompletadas() { return listarPorEstado(EstadoVenta.COMPLETADA); }

    @Override
    @Transactional
    public void eliminarVenta(Integer id) { ventaRepository.deleteById(id); }

    @Override
    @Transactional
    public void cancelarVenta(Integer id) {
        Venta venta = ventaRepository.findById(id).orElseThrow();
        for(DetalleVenta det : venta.getDetalles()) {
            Producto p = det.getProducto();
            p.setStockActual(p.getStockActual() + det.getCantidad());
            productoRepository.save(p);
        }
        venta.setEstado(EstadoVenta.CANCELADA);
        ventaRepository.save(venta);
    }

    @Override public VentaResponseDTO buscarPorCodigo(String codigo) { return convertirAResponseDTO(ventaRepository.findByCodigo(codigo).orElseThrow()); }
    @Override public List<VentaResponseDTO> buscarPorCliente(String nombre) { return ventaRepository.buscarPorCliente(nombre).stream().map(this::convertirAResponseDTO).collect(Collectors.toList()); }
    @Override public Long contarVentasPorEstado(EstadoVenta estado) { return ventaRepository.contarPorEstado(estado); }
    @Override public List<VentaResponseDTO> listarPorFecha(LocalDateTime i, LocalDateTime f) { return new ArrayList<>(); }
    @Override public VentaResponseDTO convertirBorradorAVenta(Integer id) { return obtenerVenta(id); }
    @Override public VentaResponseDTO completarVenta(Integer id) { return obtenerVenta(id); }

    // ==========================================
    // HELPERS
    // ==========================================
    private BigDecimal normalizarMonto(BigDecimal montoPago, String monedaPago, String monedaVenta, BigDecimal tipoCambio) {
        if (monedaPago.equals(monedaVenta)) return montoPago;
        if ("USD".equals(monedaPago) && "PEN".equals(monedaVenta)) return montoPago.multiply(tipoCambio).setScale(2, RoundingMode.HALF_UP);
        if ("PEN".equals(monedaPago) && "USD".equals(monedaVenta)) {
            if (tipoCambio.compareTo(BigDecimal.ZERO) == 0) return montoPago;
            return montoPago.divide(tipoCambio, 2, RoundingMode.HALF_UP);
        }
        return montoPago;
    }

    private String generarCodigoVenta() { return "VTA-" + System.currentTimeMillis(); }

    private VentaResponseDTO convertirAResponseDTO(Venta venta) {
        VentaResponseDTO dto = new VentaResponseDTO();
        dto.setId(venta.getId());
        dto.setCodigo(venta.getCodigo());
        dto.setFechaVenta(venta.getFechaVenta());
        dto.setNombreCliente(venta.getNombreCliente());
        dto.setTipoCliente(venta.getTipoCliente());
        dto.setEstado(venta.getEstado());
        dto.setTipoPago(venta.getTipoPago());

        dto.setMoneda(venta.getMoneda());
        dto.setTotal(venta.getTotal());
        dto.setSaldoPendiente(venta.getSaldoPendiente());
        dto.setNotas(venta.getNotas());

        // Detalles
        List<DetalleVentaResponseDTO> detDTOs = venta.getDetalles().stream().map(d -> {
            DetalleVentaResponseDTO dd = new DetalleVentaResponseDTO();
            dd.setProductoNombre(d.getProducto().getNombre());
            dd.setCantidad(d.getCantidad());
            dd.setSubtotal(d.getSubtotal());
            return dd;
        }).collect(Collectors.toList());
        dto.setDetalles(detDTOs);

        // ✅ CORRECCIÓN CRÍTICA: Enviar la lista de pagos al Frontend
        if (venta.getPagos() != null) {
            List<VentaResponseDTO.PagoResponseDTO> pagosDTO = venta.getPagos().stream().map(p -> {
                VentaResponseDTO.PagoResponseDTO pp = new VentaResponseDTO.PagoResponseDTO();
                pp.setId(p.getId());
                pp.setMonto(p.getMonto());
                pp.setMoneda(p.getMoneda());
                pp.setMetodoPago(p.getMetodoPago());
                pp.setFechaPago(p.getFechaPago().toString());
                pp.setReferencia(p.getReferencia());
                if (p.getCuentaDestino() != null) {
                    pp.setNombreCuentaDestino(p.getCuentaDestino().getTitular() + " - " + p.getCuentaDestino().getBanco());
                }
                return pp;
            }).collect(Collectors.toList());
            dto.setPagos(pagosDTO);
        }

        return dto;
    }
}