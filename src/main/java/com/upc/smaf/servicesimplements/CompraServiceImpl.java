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
        // 1. Validar Proveedor
        Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + request.getProveedorId()));

        // 2. Convertir Enums
        TipoCompra tipoCompra;
        TipoComprobante tipoComprobante;
        try {
            tipoCompra = TipoCompra.valueOf(request.getTipoCompra());
            tipoComprobante = TipoComprobante.valueOf(request.getTipoComprobante());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tipo de Compra o Comprobante inválido");
        }

        // 3. Crear Cabecera de Compra
        Compra compra = new Compra();
        compra.setTipoCompra(tipoCompra);
        compra.setTipoComprobante(tipoComprobante);
        compra.setTipoPago(request.getTipoPago());

        compra.setSerie(request.getSerie());
        compra.setNumero(request.getNumero());

        compra.setFechaEmision(request.getFechaEmision());
        compra.setFechaVencimiento(request.getFechaVencimiento());
        compra.setFechaRegistro(LocalDateTime.now()); // Auditoría

        compra.setProveedor(proveedor);
        compra.setMoneda(request.getMoneda());
        compra.setTipoCambio(request.getTipoCambio());
        compra.setObservaciones(request.getObservaciones());

        // Montos Base
        compra.setSubTotal(request.getSubTotal());
        compra.setIgv(request.getIgv());
        compra.setTotal(request.getTotal());

        // Impuestos adicionales
        compra.setPercepcion(request.getPercepcion() != null ? request.getPercepcion() : BigDecimal.ZERO);
        compra.setDetraccionPorcentaje(request.getDetraccionPorcentaje() != null ? request.getDetraccionPorcentaje() : BigDecimal.ZERO);
        compra.setDetraccionMonto(request.getDetraccionMonto() != null ? request.getDetraccionMonto() : BigDecimal.ZERO);
        compra.setRetencion(request.getRetencion() != null ? request.getRetencion() : BigDecimal.ZERO);

        // ====================================================
        // ✅ 3.1. NUEVA LÓGICA DE IMPORTACIÓN Y VINCULACIÓN
        // ====================================================
        compra.setCodImportacion(request.getCodImportacion());
        compra.setPesoNetoKg(request.getPesoNetoKg() != null ? request.getPesoNetoKg() : BigDecimal.ZERO);
        compra.setBultos(request.getBultos() != null ? request.getBultos() : 0);

        // Si es una importación (tiene código), buscamos o creamos la carpeta
        if (request.getCodImportacion() != null && !request.getCodImportacion().trim().isEmpty()) {
            Optional<Importacion> importacionOpt = importacionRepository.findByCodigoAgrupador(request.getCodImportacion());

            if (importacionOpt.isPresent()) {
                // Caso A: La carpeta ya existe, vinculamos la compra a ella
                compra.setImportacion(importacionOpt.get());
            } else {
                // Caso B: Es la primera factura con este código, creamos la carpeta automática
                Importacion nuevaImp = new Importacion();
                nuevaImp.setCodigoAgrupador(request.getCodImportacion());
                nuevaImp.setEstado(EstadoImportacion.ORDENADO);

                // Inicializamos valores en cero para evitar null pointers
                nuevaImp.setSumaFobTotal(BigDecimal.ZERO);
                nuevaImp.setPesoTotalKg(BigDecimal.ZERO);
                nuevaImp.setTotalFleteInternacional(BigDecimal.ZERO);
                nuevaImp.setTotalSeguro(BigDecimal.ZERO);
                nuevaImp.setTotalGastosAduana(BigDecimal.ZERO);
                nuevaImp.setTotalGastosAlmacen(BigDecimal.ZERO);
                nuevaImp.setTotalTransporteLocal(BigDecimal.ZERO);
                nuevaImp.setOtrosGastosGlobales(BigDecimal.ZERO);

                Importacion impGuardada = importacionRepository.save(nuevaImp);
                compra.setImportacion(impGuardada);
            }
        }

        // ====================================================
        // 4. PROCESAMIENTO DE PAGOS INICIALES
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
                            .orElseThrow(() -> new RuntimeException("Cuenta bancaria origen no encontrada"));
                    pago.setCuentaOrigen(cuenta);
                }

                BigDecimal montoNorm = normalizarMonto(pago.getMonto(), pago.getMoneda(), compra.getMoneda(), compra.getTipoCambio());
                totalPagadoNormalizado = totalPagadoNormalizado.add(montoNorm);

                compra.agregarPago(pago);
            }
        }

        compra.setMontoPagadoInicial(totalPagadoNormalizado);

        // Calcular Saldo Pendiente
        BigDecimal saldo = compra.getTotal().subtract(totalPagadoNormalizado);
        if (saldo.compareTo(BigDecimal.ZERO) < 0) saldo = BigDecimal.ZERO;
        compra.setSaldoPendiente(saldo);

        // Definir Estado Inicial
        if (compra.getTipoPago() == TipoPago.CONTADO) {
            if (compra.getSaldoPendiente().compareTo(new BigDecimal("0.10")) > 0) {
                compra.setEstado(EstadoCompra.REGISTRADA);
            } else {
                compra.setEstado(EstadoCompra.COMPLETADA);
            }
        } else {
            compra.setEstado(saldo.compareTo(BigDecimal.ZERO) <= 0 ? EstadoCompra.COMPLETADA : EstadoCompra.REGISTRADA);
        }

        compra.setActivo(true);

        // Guardar Cabecera
        Compra savedCompra = compraRepository.save(compra);

        // 6. PROCESAR DETALLES Y STOCK
        if (request.getDetalles() != null) {
            for (CompraRequestDTO.DetalleRequestDTO detReq : request.getDetalles()) {
                Producto producto = productoRepository.findById(detReq.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado ID: " + detReq.getProductoId()));

                CompraDetalle detalle = new CompraDetalle();
                detalle.setCompra(savedCompra);
                detalle.setProducto(producto);
                detalle.setCantidad(detReq.getCantidad() != null ? detReq.getCantidad() : 0);
                detalle.setPrecioUnitario(detReq.getPrecioUnitario());

                detalle.calcularImporte();

                if (tipoCompra == TipoCompra.BIEN) {
                    if (detReq.getAlmacenId() != null) {
                        // Ojo con el cast a long si tu repo usa Long, si usa Integer quita el .longValue()
                        Almacen almacen = almacenRepository.findById(detReq.getAlmacenId().longValue())
                                .orElseThrow(() -> new RuntimeException("Almacén no encontrado ID: " + detReq.getAlmacenId()));
                        detalle.setAlmacen(almacen);
                    }

                    // Actualizar Stock del Producto
                    int stockActual = producto.getStockActual() != null ? producto.getStockActual() : 0;
                    producto.setStockActual(stockActual + detReq.getCantidad());

                    // Actualizar Costos
                    BigDecimal costoConIGV = detReq.getPrecioUnitario().multiply(new BigDecimal("1.18")).setScale(2, RoundingMode.HALF_UP);

                    if(savedCompra.getCodImportacion() != null) {
                        producto.setPrecioChina(detReq.getPrecioUnitario());
                    }

                    producto.setCostoTotal(costoConIGV);
                    productoRepository.save(producto);
                }
                detalleRepository.save(detalle);
            }
        }

        // Si se vinculó a una importación, actualizar los totales de la carpeta
        if (savedCompra.getImportacion() != null) {
            actualizarTotalesImportacion(savedCompra.getImportacion());
        }

        return obtenerCompra(savedCompra.getId());
    }

    @Override
    @Transactional
    public CompraResponseDTO registrarAmortizacion(Integer compraId, BigDecimal monto, MetodoPago metodo, Integer cuentaOrigenId, String referencia) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));

        if (compra.getEstado() == EstadoCompra.ANULADA) {
            throw new RuntimeException("No se pueden amortizar compras anuladas");
        }

        if (compra.getSaldoPendiente().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("La compra ya está pagada completamente");
        }

        PagoCompra pago = new PagoCompra();
        pago.setMonto(monto);
        pago.setMoneda(compra.getMoneda());
        pago.setMetodoPago(metodo);
        pago.setFechaPago(LocalDateTime.now());
        pago.setReferencia(referencia);

        if (cuentaOrigenId != null) {
            CuentaBancaria cuenta = cuentaRepository.findById(cuentaOrigenId)
                    .orElseThrow(() -> new RuntimeException("Cuenta bancaria no encontrada"));
            pago.setCuentaOrigen(cuenta);
        }

        compra.agregarPago(pago);

        BigDecimal nuevoSaldo = compra.getSaldoPendiente().subtract(monto);
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) nuevoSaldo = BigDecimal.ZERO;

        compra.setSaldoPendiente(nuevoSaldo);

        if (nuevoSaldo.compareTo(BigDecimal.ZERO) <= 0) {
            compra.setEstado(EstadoCompra.COMPLETADA);
        }

        compraRepository.save(compra);
        return mapToResponseDTO(compra);
    }

    @Override
    public List<CompraResponseDTO> listarPorCodigoImportacion(String codImportacion) {
        return compraRepository.findByCodImportacion(codImportacion).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CompraResponseDTO obtenerCompra(Integer id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));
        return mapToResponseDTO(compra);
    }

    @Override
    public List<CompraResponseDTO> listarTodas() {
        return compraRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CompraResponseDTO> listarPorProveedor(Integer pid) {
        return compraRepository.findByProveedorId(pid).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CompraResponseDTO> buscarPorNumero(String num) {
        return compraRepository.buscarPorNumero(num).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void anularCompra(Integer id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));

        if(compra.getEstado() == EstadoCompra.ANULADA) return;

        if (compra.getTipoCompra() == TipoCompra.BIEN) {
            for (CompraDetalle detalle : compra.getDetalles()) {
                Producto prod = detalle.getProducto();
                int stockActual = prod.getStockActual() != null ? prod.getStockActual() : 0;
                prod.setStockActual(Math.max(0, stockActual - detalle.getCantidad()));
                productoRepository.save(prod);
            }
        }

        compra.setEstado(EstadoCompra.ANULADA);
        compra.setActivo(false);
        compraRepository.save(compra);
    }

    // ==========================================
    // MÉTODOS AUXILIARES Y MAPEOS
    // ==========================================

    private void actualizarTotalesImportacion(Importacion imp) {
        BigDecimal sumaFob = BigDecimal.ZERO;
        BigDecimal sumaPeso = BigDecimal.ZERO;

        List<Compra> facturas = compraRepository.findByCodImportacion(imp.getCodigoAgrupador());

        for (Compra c : facturas) {
            if (c.getEstado() != EstadoCompra.ANULADA) {
                sumaFob = sumaFob.add(c.getTotal());
                sumaPeso = sumaPeso.add(c.getPesoNetoKg());
            }
        }

        imp.setSumaFobTotal(sumaFob);
        imp.setPesoTotalKg(sumaPeso);
        importacionRepository.save(imp);
    }

    private BigDecimal normalizarMonto(BigDecimal monto, String monedaOrigen, String monedaDestino, BigDecimal tipoCambio) {
        if (monedaOrigen.equals(monedaDestino)) {
            return monto;
        }
        if (tipoCambio == null || tipoCambio.compareTo(BigDecimal.ZERO) == 0) {
            return monto;
        }
        if ("USD".equals(monedaOrigen) && "PEN".equals(monedaDestino)) {
            return monto.multiply(tipoCambio);
        } else if ("PEN".equals(monedaOrigen) && "USD".equals(monedaDestino)) {
            return monto.divide(tipoCambio, 2, RoundingMode.HALF_UP);
        }
        return monto;
    }

    private CompraResponseDTO mapToResponseDTO(Compra c) {
        CompraResponseDTO dto = new CompraResponseDTO();
        dto.setId(c.getId());
        dto.setSerie(c.getSerie());
        dto.setNumero(c.getNumero());

        dto.setTipoCompra(c.getTipoCompra().name());
        dto.setTipoComprobante(c.getTipoComprobante().name());
        dto.setTipoPago(c.getTipoPago().name());
        dto.setEstado(c.getEstado().name());

        dto.setFechaEmision(c.getFechaEmision());
        dto.setFechaVencimiento(c.getFechaVencimiento());
        dto.setFechaRegistro(c.getFechaRegistro());

        if (c.getProveedor() != null) {
            dto.setNombreProveedor(c.getProveedor().getNombre());
            dto.setRucProveedor(c.getProveedor().getRuc());
        }

        dto.setMoneda(c.getMoneda());
        dto.setTipoCambio(c.getTipoCambio());
        dto.setSubTotal(c.getSubTotal());
        dto.setIgv(c.getIgv());
        dto.setTotal(c.getTotal());

        dto.setSaldoPendiente(c.getSaldoPendiente());
        dto.setMontoPagado(c.getMontoPagadoInicial());

        dto.setPercepcion(c.getPercepcion());
        dto.setDetraccionPorcentaje(c.getDetraccionPorcentaje());
        dto.setDetraccionMonto(c.getDetraccionMonto());
        dto.setRetencion(c.getRetencion());

        // Datos de Importación
        dto.setCodImportacion(c.getCodImportacion());
        dto.setPesoNetoKg(c.getPesoNetoKg());
        dto.setBultos(c.getBultos());

        if (c.getImportacion() != null) {
            dto.setImportacionId(c.getImportacion().getId());
        }

        // Costos Prorrateados
        dto.setCostoTotalImportacion(c.getCostoTotalImportacion());
        dto.setProrrateoFlete(c.getProrrateoFlete());
        dto.setProrrateoSeguro(c.getProrrateoSeguro());
        dto.setProrrateoGastosAduanas(c.getProrrateoGastosAduanas());

        // Mapear Detalles
        if (c.getDetalles() != null) {
            List<CompraDetalleResponseDTO> detallesDTO = c.getDetalles().stream().map(d -> {
                CompraDetalleResponseDTO det = new CompraDetalleResponseDTO();
                det.setId(d.getId());
                det.setProductoId(d.getProducto().getId());
                det.setNombreProducto(d.getProducto().getNombre());
                det.setCodigoProducto(d.getProducto().getCodigo());
                det.setCantidad(d.getCantidad());
                det.setPrecioUnitario(d.getPrecioUnitario());

                // Cálculo seguro del importe
                BigDecimal importeCalc = d.getPrecioUnitario().multiply(new BigDecimal(d.getCantidad()));
                det.setImporte(importeCalc);

                if(d.getAlmacen() != null) {
                    det.setNombreAlmacen(d.getAlmacen().getNombre());
                }
                return det;
            }).collect(Collectors.toList());
            dto.setDetalles(detallesDTO);
        }

        // Mapear Pagos
        if (c.getPagos() != null) {
            List<PagoCompraResponseDTO> pagosDTO = c.getPagos().stream().map(p -> {
                PagoCompraResponseDTO pay = new PagoCompraResponseDTO();
                pay.setId(p.getId());
                pay.setMonto(p.getMonto());
                pay.setMoneda(p.getMoneda());
                pay.setMetodoPago(p.getMetodoPago().name());
                pay.setFechaPago(p.getFechaPago());
                pay.setReferencia(p.getReferencia());
                return pay;
            }).collect(Collectors.toList());
            dto.setPagos(pagosDTO);
        } else {
            dto.setPagos(Collections.emptyList());
        }

        return dto;
    }
}