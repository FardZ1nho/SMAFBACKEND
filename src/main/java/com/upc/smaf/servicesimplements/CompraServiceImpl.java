package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.CompraRequestDTO;
import com.upc.smaf.dtos.response.CompraDetalleResponseDTO;
import com.upc.smaf.dtos.response.CompraResponseDTO;
import com.upc.smaf.entities.*;
import com.upc.smaf.repositories.*;
import com.upc.smaf.serviceinterface.CompraService;
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
public class CompraServiceImpl implements CompraService {

    private final CompraRepository compraRepository;
    private final CompraDetalleRepository detalleRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final AlmacenRepository almacenRepository;
    private final ImportacionRepository importacionRepository;

    // ✅ Repositorios necesarios para pagos
    private final CuentaBancariaRepository cuentaRepository;

    @Override
    @Transactional
    public CompraResponseDTO registrarCompra(CompraRequestDTO request) {
        // 1. Validar Proveedor
        Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        // 2. Enums
        TipoCompra tipoCompra = TipoCompra.valueOf(request.getTipoCompra());
        TipoComprobante tipoComprobante = TipoComprobante.valueOf(request.getTipoComprobante());

        // 3. Crear Cabecera
        Compra compra = new Compra();
        compra.setTipoCompra(tipoCompra);
        compra.setTipoComprobante(tipoComprobante);
        compra.setTipoPago(request.getTipoPago());

        compra.setSerie(request.getSerie());
        compra.setNumero(request.getNumero());

        // ✅ AHORA SÍ: GUARDAMOS EL CÓDIGO DE IMPORTACIÓN
        compra.setCodImportacion(request.getCodImportacion());

        compra.setFechaEmision(request.getFechaEmision());
        compra.setFechaVencimiento(request.getFechaVencimiento());
        compra.setProveedor(proveedor);
        compra.setMoneda(request.getMoneda());
        compra.setTipoCambio(request.getTipoCambio());
        compra.setObservaciones(request.getObservaciones());

        compra.setSubTotal(request.getSubTotal());
        compra.setIgv(request.getIgv());
        compra.setTotal(request.getTotal());

        compra.setPercepcion(request.getPercepcion());
        compra.setDetraccionPorcentaje(request.getDetraccionPorcentaje());
        compra.setDetraccionMonto(request.getDetraccionMonto());
        compra.setRetencion(request.getRetencion());

        // ====================================================
        // ✅ 4. PROCESAMIENTO DE PAGOS
        // ====================================================
        BigDecimal totalPagadoNormalizado = BigDecimal.ZERO;

        if (request.getPagos() != null && !request.getPagos().isEmpty()) {
            for (CompraRequestDTO.PagoCompraRequestDTO pagoDTO : request.getPagos()) {
                PagoCompra pago = new PagoCompra();
                pago.setMonto(pagoDTO.getMonto());
                pago.setMoneda(pagoDTO.getMoneda());
                pago.setMetodoPago(pagoDTO.getMetodoPago());
                pago.setFechaPago(LocalDateTime.now());
                pago.setReferencia(pagoDTO.getReferencia());

                if (pagoDTO.getCuentaOrigenId() != null) {
                    CuentaBancaria cuenta = cuentaRepository.findById(pagoDTO.getCuentaOrigenId())
                            .orElseThrow(() -> new RuntimeException("Cuenta de origen no encontrada"));
                    pago.setCuentaOrigen(cuenta);
                }

                // Normalizar monto a la moneda de la compra para calcular deuda
                BigDecimal montoNorm = normalizarMonto(pago.getMonto(), pago.getMoneda(), compra.getMoneda(), compra.getTipoCambio());
                totalPagadoNormalizado = totalPagadoNormalizado.add(montoNorm);

                compra.agregarPago(pago); // Vinculación bidireccional
            }
        }

        compra.setMontoPagadoInicial(totalPagadoNormalizado);

        // Calcular Saldo
        BigDecimal saldo = compra.getTotal().subtract(totalPagadoNormalizado);
        if (saldo.compareTo(BigDecimal.ZERO) < 0) saldo = BigDecimal.ZERO;
        compra.setSaldoPendiente(saldo);

        // Validar y Asignar Estado
        if (compra.getTipoPago() == TipoPago.CONTADO) {
            // Tolerancia de 0.10 céntimos
            if (compra.getSaldoPendiente().compareTo(new BigDecimal("0.10")) > 0) {
                throw new RuntimeException("Compra al CONTADO debe ser pagada en su totalidad.");
            }
            compra.setEstado(EstadoCompra.COMPLETADA);
        } else {
            compra.setEstado(saldo.compareTo(BigDecimal.ZERO) <= 0 ? EstadoCompra.COMPLETADA : EstadoCompra.REGISTRADA);
        }

        Compra savedCompra = compraRepository.save(compra);

        // ====================================================
        // 5. IMPORTACIÓN AUTOMÁTICA
        // ====================================================
        if (savedCompra.getTipoComprobante() == TipoComprobante.FACTURA_COMERCIAL) {
            Importacion imp = new Importacion();
            imp.setCompra(savedCompra);
            imp.setEstado(EstadoImportacion.ORDENADO);
            imp.setCostoFlete(BigDecimal.ZERO);
            imp.setCostoSeguro(BigDecimal.ZERO);
            imp.setImpuestosAduanas(BigDecimal.ZERO);
            imp.setGastosOperativos(BigDecimal.ZERO);
            importacionRepository.save(imp);
        }

        // 6. PROCESAR DETALLES Y STOCK
        if (request.getDetalles() != null) {
            for (CompraRequestDTO.DetalleRequestDTO detReq : request.getDetalles()) {
                Producto producto = productoRepository.findById(detReq.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                CompraDetalle detalle = new CompraDetalle();
                detalle.setCompra(savedCompra);
                detalle.setProducto(producto);
                detalle.setCantidad(detReq.getCantidad() != null ? detReq.getCantidad() : 0);
                detalle.setPrecioUnitario(detReq.getPrecioUnitario());
                detalle.calcularImporte();

                if (tipoCompra == TipoCompra.BIEN) {
                    if (detReq.getAlmacenId() == null) throw new RuntimeException("Almacén obligatorio para Bienes");
                    Almacen almacen = almacenRepository.findById(detReq.getAlmacenId().longValue())
                            .orElseThrow(() -> new RuntimeException("Almacén no encontrado"));
                    detalle.setAlmacen(almacen);

                    // Stock
                    int stockActual = producto.getStockActual() != null ? producto.getStockActual() : 0;
                    producto.setStockActual(stockActual + detReq.getCantidad());

                    // Costos
                    BigDecimal costoConIGV = detReq.getPrecioUnitario().multiply(new BigDecimal("1.18")).setScale(2, RoundingMode.HALF_UP);
                    producto.setCostoTotal(costoConIGV);
                    producto.setPrecioChina(detReq.getPrecioUnitario());
                    productoRepository.save(producto);
                }
                detalleRepository.save(detalle);
            }
        }

        return obtenerCompra(savedCompra.getId());
    }

    // ✅ Método para Amortizar Deuda
    @Override
    @Transactional
    public CompraResponseDTO registrarAmortizacion(Integer compraId, BigDecimal monto, MetodoPago metodo, Integer cuentaOrigenId, String referencia) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));

        if (compra.getSaldoPendiente().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Esta compra ya está pagada totalmente.");
        }

        PagoCompra pago = new PagoCompra();
        pago.setMonto(monto);
        pago.setMoneda(compra.getMoneda());
        pago.setMetodoPago(metodo);
        pago.setFechaPago(LocalDateTime.now());
        pago.setReferencia(referencia);

        if (cuentaOrigenId != null) {
            CuentaBancaria cuenta = cuentaRepository.findById(cuentaOrigenId)
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
            pago.setCuentaOrigen(cuenta);
        }

        compra.agregarPago(pago);

        // Actualizar saldo
        compra.setSaldoPendiente(compra.getSaldoPendiente().subtract(monto));

        if (compra.getSaldoPendiente().compareTo(BigDecimal.ZERO) <= 0) {
            compra.setSaldoPendiente(BigDecimal.ZERO);
            compra.setEstado(EstadoCompra.COMPLETADA);
        }

        Compra saved = compraRepository.save(compra);
        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CompraResponseDTO obtenerCompra(Integer id) {
        return mapToResponseDTO(compraRepository.findById(id).orElseThrow(() -> new RuntimeException("No encontrado")));
    }

    @Override public List<CompraResponseDTO> listarTodas() { return compraRepository.findAll().stream().map(this::mapToResponseDTO).collect(Collectors.toList()); }
    @Override public List<CompraResponseDTO> listarPorProveedor(Integer pid) { return compraRepository.findByProveedorId(pid).stream().map(this::mapToResponseDTO).collect(Collectors.toList()); }
    @Override public List<CompraResponseDTO> buscarPorNumero(String num) { return compraRepository.buscarPorNumero(num).stream().map(this::mapToResponseDTO).collect(Collectors.toList()); }

    // --- HELPERS ---
    private BigDecimal normalizarMonto(BigDecimal montoPago, String monedaPago, String monedaCompra, BigDecimal tipoCambio) {
        if (monedaPago.equals(monedaCompra)) return montoPago;
        if ("USD".equals(monedaPago) && "PEN".equals(monedaCompra)) return montoPago.multiply(tipoCambio).setScale(2, RoundingMode.HALF_UP);
        if ("PEN".equals(monedaPago) && "USD".equals(monedaCompra)) {
            if (tipoCambio.compareTo(BigDecimal.ZERO) == 0) return montoPago;
            return montoPago.divide(tipoCambio, 2, RoundingMode.HALF_UP);
        }
        return montoPago;
    }

    private CompraResponseDTO mapToResponseDTO(Compra c) {
        CompraResponseDTO d = new CompraResponseDTO();
        d.setId(c.getId());
        d.setTipoCompra(c.getTipoCompra().name());
        d.setTipoComprobante(c.getTipoComprobante().name());
        d.setTipoPago(c.getTipoPago().name());
        d.setEstado(c.getEstado().name());
        d.setSerie(c.getSerie());
        d.setNumero(c.getNumero());

        // ✅ AHORA SÍ: DEVOLVEMOS EL CÓDIGO DE IMPORTACIÓN AL FRONTEND
        d.setCodImportacion(c.getCodImportacion());

        d.setFechaEmision(c.getFechaEmision());
        d.setFechaVencimiento(c.getFechaVencimiento());
        d.setFechaRegistro(c.getFechaRegistro());

        if(c.getProveedor() != null) {
            d.setNombreProveedor(c.getProveedor().getNombre());
            d.setRucProveedor(c.getProveedor().getRuc());
        }

        d.setMoneda(c.getMoneda());
        d.setTipoCambio(c.getTipoCambio());
        d.setSubTotal(c.getSubTotal());
        d.setIgv(c.getIgv());
        d.setTotal(c.getTotal());
        d.setMontoPagadoInicial(c.getMontoPagadoInicial());
        d.setSaldoPendiente(c.getSaldoPendiente());
        d.setObservaciones(c.getObservaciones());

        if (c.getDetalles() != null) {
            d.setDetalles(c.getDetalles().stream().map(det -> {
                CompraDetalleResponseDTO dr = new CompraDetalleResponseDTO();
                dr.setId(det.getId());
                if(det.getProducto()!=null) {
                    dr.setProductoId(det.getProducto().getId());
                    dr.setNombreProducto(det.getProducto().getNombre());
                }
                dr.setCantidad(det.getCantidad());
                dr.setPrecioUnitario(det.getPrecioUnitario());
                dr.setImporteTotal(det.getImporteTotal());
                return dr;
            }).collect(Collectors.toList()));
        }

        // ✅ MAPEO DE PAGOS AL FRONTEND
        if (c.getPagos() != null) {
            d.setPagos(c.getPagos().stream().map(p -> {
                CompraResponseDTO.PagoCompraResponseDTO pr = new CompraResponseDTO.PagoCompraResponseDTO();
                pr.setId(p.getId());
                pr.setMonto(p.getMonto());
                pr.setMoneda(p.getMoneda());
                pr.setMetodoPago(p.getMetodoPago().name());
                pr.setFechaPago(p.getFechaPago().toString());
                pr.setReferencia(p.getReferencia());
                if(p.getCuentaOrigen() != null) {
                    pr.setNombreCuentaOrigen(p.getCuentaOrigen().getNombre() + " (" + p.getCuentaOrigen().getBanco() + ")");
                }
                return pr;
            }).collect(Collectors.toList()));
        }
        return d;
    }
}