package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.CompraRequestDTO;
import com.upc.smaf.dtos.response.CompraDetalleResponseDTO;
import com.upc.smaf.dtos.response.CompraResponseDTO;
import com.upc.smaf.dtos.response.PagoCompraResponseDTO;
import com.upc.smaf.entities.*;
import com.upc.smaf.repositories.*;
import com.upc.smaf.serviceinterface.CompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    private final CuentaBancariaRepository cuentaRepository;

    @Override
    @Transactional
    public CompraResponseDTO registrarCompra(CompraRequestDTO request) {
        Compra compra = new Compra();

        // ====================================================
        // 1. GESTIÓN DEL PROVEEDOR (BD o Libre)
        // ====================================================
        if (request.getProveedorId() != null) {
            Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                    .orElseThrow(() -> new RuntimeException("Proveedor no encontrado ID: " + request.getProveedorId()));
            compra.setProveedor(proveedor);
            compra.setNombreProveedor(proveedor.getNombre());
        } else {
            // Es un proveedor de texto libre
            compra.setProveedor(null);
            compra.setNombreProveedor(request.getNombreProveedor());
        }

        compra.setTipoCompra(TipoCompra.valueOf(request.getTipoCompra()));
        compra.setTipoComprobante(TipoComprobante.valueOf(request.getTipoComprobante()));
        compra.setTipoPago(request.getTipoPago());

        compra.setSerie(request.getSerie());
        compra.setNumero(request.getNumero());
        compra.setFechaEmision(request.getFechaEmision());
        compra.setFechaVencimiento(request.getFechaVencimiento());
        compra.setFechaRegistro(LocalDateTime.now());
        compra.setMoneda(request.getMoneda());
        compra.setTipoCambio(request.getTipoCambio());
        compra.setObservaciones(request.getObservaciones());

        // 2. TOTALES
        compra.setSubTotal(request.getSubTotal());
        compra.setFob(request.getFob() != null ? request.getFob() : BigDecimal.ZERO);
        compra.setIgv(request.getIgv());
        compra.setTotal(request.getTotal());

        // Impuestos
        compra.setPercepcion(request.getPercepcion() != null ? request.getPercepcion() : BigDecimal.ZERO);
        compra.setDetraccionPorcentaje(request.getDetraccionPorcentaje());
        compra.setDetraccionMonto(request.getDetraccionMonto());
        compra.setRetencion(request.getRetencion());

        // 3. LÓGICA DE IMPORTACIÓN
        compra.setCodImportacion(request.getCodImportacion());
        compra.setPesoNetoKg(request.getPesoNetoKg() != null ? request.getPesoNetoKg() : BigDecimal.ZERO);
        compra.setCbm(request.getCbm() != null ? request.getCbm() : BigDecimal.ZERO);

        if (request.getCodImportacion() != null && !request.getCodImportacion().trim().isEmpty()) {
            Optional<Importacion> importacionOpt = importacionRepository.findByCodigoAgrupador(request.getCodImportacion());

            if (importacionOpt.isPresent()) {
                compra.setImportacion(importacionOpt.get());
            } else {
                Importacion nuevaImp = new Importacion();
                nuevaImp.setCodigoAgrupador(request.getCodImportacion());
                nuevaImp.setEstado(EstadoImportacion.ORDENADO);
                nuevaImp.setSumaFobTotal(BigDecimal.ZERO);
                nuevaImp.setPesoTotalKg(BigDecimal.ZERO);
                nuevaImp.setCbmTotal(BigDecimal.ZERO);
                nuevaImp.setCostoFlete(BigDecimal.ZERO);
                nuevaImp.setCostoAlmacenajeCft(BigDecimal.ZERO);

                Importacion impGuardada = importacionRepository.save(nuevaImp);
                compra.setImportacion(impGuardada);
            }
        }

        // 4. PAGOS
        BigDecimal totalPagadoNormalizado = BigDecimal.ZERO;
        if (request.getPagos() != null) {
            for (CompraRequestDTO.PagoCompraRequestDTO pagoDTO : request.getPagos()) {
                PagoCompra pago = new PagoCompra();
                pago.setMonto(pagoDTO.getMonto());
                pago.setMoneda(pagoDTO.getMoneda());
                pago.setMetodoPago(pagoDTO.getMetodoPago());
                pago.setFechaPago(LocalDateTime.now());
                pago.setReferencia(pagoDTO.getReferencia());
                if (pagoDTO.getCuentaOrigenId() != null) {
                    pago.setCuentaOrigen(cuentaRepository.findById(pagoDTO.getCuentaOrigenId()).orElse(null));
                }

                BigDecimal montoNorm = pago.getMonto();
                if(!pago.getMoneda().equals(compra.getMoneda()) && compra.getTipoCambio() != null) {
                    if ("USD".equals(pago.getMoneda())) {
                        montoNorm = montoNorm.multiply(compra.getTipoCambio());
                    } else if ("USD".equals(compra.getMoneda())) {
                        montoNorm = montoNorm.divide(compra.getTipoCambio(), 2, RoundingMode.HALF_UP);
                    }
                }
                totalPagadoNormalizado = totalPagadoNormalizado.add(montoNorm);
                compra.agregarPago(pago);
            }
        }
        compra.setMontoPagadoInicial(totalPagadoNormalizado);

        BigDecimal saldo = compra.getTotal().subtract(totalPagadoNormalizado);
        compra.setSaldoPendiente(saldo.max(BigDecimal.ZERO));
        compra.setEstado((saldo.compareTo(BigDecimal.ZERO) <= 0) ? EstadoCompra.COMPLETADA : EstadoCompra.REGISTRADA);

        Compra savedCompra = compraRepository.save(compra);

        // ====================================================
        // 5. DETALLES DE PRODUCTOS (BD o Libres)
        // ====================================================
        if (request.getDetalles() != null) {
            for (CompraRequestDTO.DetalleRequestDTO detReq : request.getDetalles()) {
                CompraDetalle detalle = new CompraDetalle();
                detalle.setCompra(savedCompra);
                detalle.setCantidad(detReq.getCantidad());
                detalle.setCantidadRecibida(0);
                detalle.setPrecioUnitario(detReq.getPrecioUnitario());
                detalle.calcularImporte();

                if (detReq.getAlmacenId() != null) {
                    detalle.setAlmacen(almacenRepository.findById(detReq.getAlmacenId().longValue()).orElse(null));
                }

                if (detReq.getProductoId() != null) {
                    Producto producto = productoRepository.findById(detReq.getProductoId())
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                    detalle.setProducto(producto);
                    detalle.setNombreProducto(producto.getNombre());

                    if(compra.getTipoCompra() == TipoCompra.BIEN && compra.getTipoComprobante() != TipoComprobante.FACTURA_COMERCIAL) {
                        int stock = producto.getStockActual() != null ? producto.getStockActual() : 0;
                        producto.setStockActual(stock + detReq.getCantidad());
                        producto.setCostoTotal(detReq.getPrecioUnitario());
                        productoRepository.save(producto);
                    } else if (compra.getTipoComprobante() == TipoComprobante.FACTURA_COMERCIAL) {
                        producto.setPrecioChina(detReq.getPrecioUnitario());
                        productoRepository.save(producto);
                    }
                } else {
                    // ES UN ÍTEM DE TEXTO LIBRE
                    detalle.setProducto(null);
                    detalle.setNombreProducto(detReq.getNombreProducto());
                }

                detalleRepository.save(detalle);
            }
        }

        if (savedCompra.getImportacion() != null) {
            actualizarTotalesImportacion(savedCompra.getImportacion());
        }

        return obtenerCompra(savedCompra.getId());
    }

    @Override
    @Transactional
    public CompraResponseDTO registrarAmortizacion(Integer compraId, BigDecimal monto, MetodoPago metodo, Integer cuentaId, String referencia) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));

        PagoCompra pago = new PagoCompra();
        pago.setMonto(monto);
        pago.setMoneda(compra.getMoneda());
        pago.setMetodoPago(metodo);
        pago.setFechaPago(LocalDateTime.now());
        pago.setReferencia(referencia);

        if (cuentaId != null) {
            pago.setCuentaOrigen(cuentaRepository.findById(cuentaId).orElse(null));
        }

        compra.agregarPago(pago);

        BigDecimal pagado = compra.getPagos().stream()
                .map(PagoCompra::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        compra.setSaldoPendiente(compra.getTotal().subtract(pagado).max(BigDecimal.ZERO));

        if (compra.getSaldoPendiente().compareTo(BigDecimal.ZERO) == 0) {
            compra.setEstado(EstadoCompra.COMPLETADA);
        }

        compraRepository.save(compra);
        return mapToResponseDTO(compra);
    }

    @Override
    @Transactional
    public void anularCompra(Integer id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe compra con ID: " + id));

        if(compra.getEstado() == EstadoCompra.ANULADA) return;

        if (compra.getTipoCompra() == TipoCompra.BIEN && compra.getTipoComprobante() != TipoComprobante.FACTURA_COMERCIAL) {
            for (CompraDetalle d : compra.getDetalles()) {
                if (d.getProducto() != null) {
                    Producto p = d.getProducto();
                    p.setStockActual(Math.max(0, p.getStockActual() - d.getCantidad()));
                    productoRepository.save(p);
                }
            }
        }

        compra.setEstado(EstadoCompra.ANULADA);
        compraRepository.save(compra);

        if (compra.getImportacion() != null) {
            actualizarTotalesImportacion(compra.getImportacion());
        }
    }

    @Override
    public List<CompraResponseDTO> listarTodas() {
        return compraRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CompraResponseDTO obtenerCompra(Integer id) {
        return compraRepository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));
    }

    @Override
    public List<CompraResponseDTO> buscarPorNumero(String numero) {
        return Collections.emptyList();
    }

    @Override
    public List<CompraResponseDTO> listarPorProveedor(Integer proveedorId) {
        return compraRepository.findAll().stream()
                .filter(c -> c.getProveedor() != null && c.getProveedor().getId().equals(proveedorId))
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CompraResponseDTO> listarPorCodigoImportacion(String codImportacion) {
        return compraRepository.findByCodImportacion(codImportacion).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CompraResponseDTO actualizarCompra(Integer id, CompraRequestDTO request) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada ID: " + id));

        if (compra.getEstado() == EstadoCompra.ANULADA) {
            throw new RuntimeException("No se puede editar una compra ANULADA");
        }

        if (compra.getTipoCompra() == TipoCompra.BIEN && compra.getTipoComprobante() != TipoComprobante.FACTURA_COMERCIAL) {
            for (CompraDetalle detalleViejo : compra.getDetalles()) {
                if (detalleViejo.getProducto() != null) {
                    Producto p = detalleViejo.getProducto();
                    int stockActual = p.getStockActual() != null ? p.getStockActual() : 0;
                    p.setStockActual(Math.max(0, stockActual - detalleViejo.getCantidad()));
                    productoRepository.save(p);
                }
            }
        }

        detalleRepository.deleteAll(compra.getDetalles());
        compra.getDetalles().clear();

        // 4. ACTUALIZAR PROVEEDOR
        if (request.getProveedorId() != null) {
            Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                    .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
            compra.setProveedor(proveedor);
            compra.setNombreProveedor(proveedor.getNombre());
        } else {
            compra.setProveedor(null);
            compra.setNombreProveedor(request.getNombreProveedor());
        }

        compra.setTipoCompra(TipoCompra.valueOf(request.getTipoCompra()));
        compra.setTipoComprobante(TipoComprobante.valueOf(request.getTipoComprobante()));
        compra.setSerie(request.getSerie());
        compra.setNumero(request.getNumero());
        compra.setFechaEmision(request.getFechaEmision());
        compra.setFechaVencimiento(request.getFechaVencimiento());
        compra.setMoneda(request.getMoneda());
        compra.setTipoCambio(request.getTipoCambio());
        compra.setObservaciones(request.getObservaciones());

        compra.setSubTotal(request.getSubTotal());
        compra.setFob(request.getFob() != null ? request.getFob() : BigDecimal.ZERO);
        compra.setIgv(request.getIgv());
        compra.setTotal(request.getTotal());

        compra.setPercepcion(request.getPercepcion() != null ? request.getPercepcion() : BigDecimal.ZERO);
        compra.setRetencion(request.getRetencion());

        String codImportacionAnterior = compra.getCodImportacion();
        compra.setCodImportacion(request.getCodImportacion());
        compra.setPesoNetoKg(request.getPesoNetoKg());
        compra.setCbm(request.getCbm());

        if (request.getCodImportacion() != null && !request.getCodImportacion().equals(codImportacionAnterior)) {
            Optional<Importacion> importacionOpt = importacionRepository.findByCodigoAgrupador(request.getCodImportacion());
            if (importacionOpt.isPresent()) {
                compra.setImportacion(importacionOpt.get());
            } else {
                Importacion nuevaImp = new Importacion();
                nuevaImp.setCodigoAgrupador(request.getCodImportacion());
                nuevaImp.setEstado(EstadoImportacion.ORDENADO);
                nuevaImp.setSumaFobTotal(BigDecimal.ZERO);
                compra.setImportacion(importacionRepository.save(nuevaImp));
            }
        }

        if (request.getDetalles() != null) {
            for (CompraRequestDTO.DetalleRequestDTO detReq : request.getDetalles()) {
                CompraDetalle detalle = new CompraDetalle();
                detalle.setCompra(compra);
                detalle.setCantidad(detReq.getCantidad());
                detalle.setCantidadRecibida(0);
                detalle.setPrecioUnitario(detReq.getPrecioUnitario());
                detalle.calcularImporte();

                if (detReq.getAlmacenId() != null) {
                    detalle.setAlmacen(almacenRepository.findById(detReq.getAlmacenId().longValue()).orElse(null));
                }

                if (detReq.getProductoId() != null) {
                    Producto producto = productoRepository.findById(detReq.getProductoId())
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado ID: " + detReq.getProductoId()));

                    detalle.setProducto(producto);
                    detalle.setNombreProducto(producto.getNombre());

                    if (compra.getTipoCompra() == TipoCompra.BIEN && compra.getTipoComprobante() != TipoComprobante.FACTURA_COMERCIAL) {
                        int stock = producto.getStockActual() != null ? producto.getStockActual() : 0;
                        producto.setStockActual(stock + detReq.getCantidad());
                        producto.setCostoTotal(detReq.getPrecioUnitario());
                        productoRepository.save(producto);
                    } else if (compra.getTipoComprobante() == TipoComprobante.FACTURA_COMERCIAL) {
                        producto.setPrecioChina(detReq.getPrecioUnitario());
                        productoRepository.save(producto);
                    }
                } else {
                    detalle.setProducto(null);
                    detalle.setNombreProducto(detReq.getNombreProducto());
                }

                detalleRepository.save(detalle);
            }
        }

        BigDecimal totalPagado = compra.getMontoPagadoInicial();
        if(compra.getPagos() != null) {
            totalPagado = compra.getPagos().stream().map(PagoCompra::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        BigDecimal saldo = compra.getTotal().subtract(totalPagado);
        compra.setSaldoPendiente(saldo.max(BigDecimal.ZERO));
        compra.setEstado((saldo.compareTo(BigDecimal.ZERO) <= 0) ? EstadoCompra.COMPLETADA : EstadoCompra.REGISTRADA);

        Compra compraGuardada = compraRepository.save(compra);

        if (compraGuardada.getImportacion() != null) {
            actualizarTotalesImportacion(compraGuardada.getImportacion());
        }
        if (codImportacionAnterior != null && !codImportacionAnterior.equals(request.getCodImportacion())) {
            importacionRepository.findByCodigoAgrupador(codImportacionAnterior)
                    .ifPresent(this::actualizarTotalesImportacion);
        }

        return obtenerCompra(compraGuardada.getId());
    }

    private void actualizarTotalesImportacion(Importacion imp) {
        BigDecimal sumaFob = BigDecimal.ZERO;
        BigDecimal sumaPeso = BigDecimal.ZERO;
        BigDecimal sumaCbm = BigDecimal.ZERO;

        List<Compra> facturas = compraRepository.findByCodImportacion(imp.getCodigoAgrupador());

        for (Compra c : facturas) {
            if (c.getEstado() != EstadoCompra.ANULADA) {
                sumaFob = sumaFob.add(c.getTotal());
                sumaPeso = sumaPeso.add(orZero(c.getPesoNetoKg()));
                sumaCbm = sumaCbm.add(orZero(c.getCbm()));
            }
        }

        imp.setSumaFobTotal(sumaFob);
        imp.setPesoTotalKg(sumaPeso);
        imp.setCbmTotal(sumaCbm);
        importacionRepository.save(imp);
    }

    private CompraResponseDTO mapToResponseDTO(Compra c) {
        CompraResponseDTO dto = new CompraResponseDTO();
        dto.setId(c.getId());
        dto.setSerie(c.getSerie());
        dto.setNumero(c.getNumero());
        dto.setTipoComprobante(c.getTipoComprobante() != null ? c.getTipoComprobante().name() : "FACTURA_ELECTRONICA");
        dto.setTipoCompra(c.getTipoCompra() != null ? c.getTipoCompra().name() : "BIEN");
        dto.setTipoPago(c.getTipoPago() != null ? c.getTipoPago().name() : "CONTADO");
        dto.setFechaEmision(c.getFechaEmision());
        dto.setEstado(c.getEstado() != null ? c.getEstado().name() : "PENDIENTE");

        // ✅ MAPEO SEGURO DEL PROVEEDOR
        if (c.getProveedor() != null) {
            dto.setProveedorId(c.getProveedor().getId());
            dto.setNombreProveedor(c.getProveedor().getNombre());
            dto.setRucProveedor(c.getProveedor().getRuc());
        } else {
            dto.setProveedorId(0);
            dto.setNombreProveedor(c.getNombreProveedor() != null ? c.getNombreProveedor() : "PROVEEDOR DE TEXTO LIBRE");
            dto.setRucProveedor("S/D");
        }

        dto.setMoneda(c.getMoneda());
        dto.setTipoCambio(c.getTipoCambio());
        dto.setSubTotal(c.getSubTotal());
        dto.setFob(orZero(c.getFob()));
        dto.setIgv(orZero(c.getIgv()));
        dto.setTotal(c.getTotal());
        dto.setSaldoPendiente(c.getSaldoPendiente());

        dto.setCodImportacion(c.getCodImportacion());
        dto.setPesoNetoKg(orZero(c.getPesoNetoKg()));
        dto.setCbm(orZero(c.getCbm()));

        dto.setProFlete(orZero(c.getProFlete()));
        dto.setProAlmacenaje(orZero(c.getProAlmacenaje()));
        dto.setProTransporte(orZero(c.getProTransporte()));
        dto.setProCargaDescarga(orZero(c.getProCargaDescarga()));
        dto.setProDesconsolidacion(orZero(c.getProDesconsolidacion()));
        dto.setProGastosAduaneros(orZero(c.getProGastosAduaneros()));
        dto.setProSeguroResguardo(orZero(c.getProSeguroResguardo()));
        dto.setProImpuestos(orZero(c.getProImpuestos()));
        dto.setProOtrosGastos(orZero(c.getProOtrosGastos()));
        dto.setCostoTotalImportacion(orZero(c.getCostoTotalImportacion()));

        if (c.getDetalles() != null && !c.getDetalles().isEmpty()) {
            List<CompraDetalleResponseDTO> detallesDto = c.getDetalles().stream().map(d -> {
                CompraDetalleResponseDTO item = new CompraDetalleResponseDTO();
                item.setId(d.getId());

                if (d.getProducto() != null) {
                    item.setProductoId(d.getProducto().getId());
                    item.setCodigoProducto(d.getProducto().getCodigo());
                    item.setNombreProducto(d.getProducto().getNombre());
                } else {
                    item.setProductoId(0);
                    item.setCodigoProducto("LIBRE");
                    item.setNombreProducto(d.getNombreProducto() != null ? d.getNombreProducto() : "Ítem Libre");
                }

                item.setCantidad(d.getCantidad());
                item.setCantidadRecibida(d.getCantidadRecibida() != null ? d.getCantidadRecibida() : 0);
                item.setPrecioUnitario(d.getPrecioUnitario());

                if (d.getImporteTotal() != null) {
                    item.setImporte(d.getImporteTotal());
                } else {
                    BigDecimal cant = new BigDecimal(d.getCantidad());
                    item.setImporte(d.getPrecioUnitario().multiply(cant));
                }

                if (d.getAlmacen() != null) {
                    item.setNombreAlmacen(d.getAlmacen().getNombre());
                }

                item.setCostoUnitarioLanded(d.getCostoUnitarioLanded());
                item.setCostoTotalLanded(d.getCostoTotalLanded());

                return item;
            }).collect(Collectors.toList());

            dto.setDetalles(detallesDto);
        }

        if (c.getPagos() != null && !c.getPagos().isEmpty()) {
            List<PagoCompraResponseDTO> pagosDto = c.getPagos().stream().map(p -> {
                PagoCompraResponseDTO pago = new PagoCompraResponseDTO();
                pago.setId(p.getId());
                pago.setMonto(p.getMonto());
                pago.setMoneda(p.getMoneda());
                pago.setMetodoPago(p.getMetodoPago() != null ? p.getMetodoPago().name() : null);
                pago.setFechaPago(p.getFechaPago());
                pago.setReferencia(p.getReferencia());
                return pago;
            }).collect(Collectors.toList());

            dto.setPagos(pagosDto);
        }

        return dto;
    }

    private BigDecimal orZero(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}