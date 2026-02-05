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
        // 1. Validaciones y Enums
        Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado ID: " + request.getProveedorId()));

        Compra compra = new Compra();
        compra.setTipoCompra(TipoCompra.valueOf(request.getTipoCompra()));
        compra.setTipoComprobante(TipoComprobante.valueOf(request.getTipoComprobante()));
        compra.setTipoPago(request.getTipoPago());

        compra.setSerie(request.getSerie());
        compra.setNumero(request.getNumero());
        compra.setFechaEmision(request.getFechaEmision());
        compra.setFechaVencimiento(request.getFechaVencimiento());
        compra.setFechaRegistro(LocalDateTime.now());
        compra.setProveedor(proveedor);
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

        // ====================================================
        // ✅ 3. LÓGICA DE IMPORTACIÓN (Datos Logísticos)
        // ====================================================
        compra.setCodImportacion(request.getCodImportacion());
        compra.setPesoNetoKg(request.getPesoNetoKg() != null ? request.getPesoNetoKg() : BigDecimal.ZERO);
        compra.setCbm(request.getCbm() != null ? request.getCbm() : BigDecimal.ZERO);

        // Vincular o Crear Carpeta de Importación Automáticamente
        if (request.getCodImportacion() != null && !request.getCodImportacion().trim().isEmpty()) {
            Optional<Importacion> importacionOpt = importacionRepository.findByCodigoAgrupador(request.getCodImportacion());

            if (importacionOpt.isPresent()) {
                compra.setImportacion(importacionOpt.get());
            } else {
                Importacion nuevaImp = new Importacion();
                nuevaImp.setCodigoAgrupador(request.getCodImportacion());
                nuevaImp.setEstado(EstadoImportacion.ORDENADO);
                // Inicializar valores en CERO
                nuevaImp.setSumaFobTotal(BigDecimal.ZERO);
                nuevaImp.setPesoTotalKg(BigDecimal.ZERO);
                nuevaImp.setCbmTotal(BigDecimal.ZERO);

                // Inicializar costos globales en CERO para evitar nulos
                nuevaImp.setCostoFlete(BigDecimal.ZERO);
                nuevaImp.setCostoAlmacenajeCft(BigDecimal.ZERO);
                // ... (inicializar otros si es necesario, aunque orZero lo maneja)

                Importacion impGuardada = importacionRepository.save(nuevaImp);
                compra.setImportacion(impGuardada);
            }
        }

        // 4. PAGOS (Lógica de amortización inicial)
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

                // Normalizar para saldo (Si pagan en Soles una factura en Dólares)
                BigDecimal montoNorm = pago.getMonto();
                if(!pago.getMoneda().equals(compra.getMoneda()) && compra.getTipoCambio() != null) {
                    if ("USD".equals(pago.getMoneda())) {
                        montoNorm = montoNorm.multiply(compra.getTipoCambio()); // Dólar a Sol (aprox)
                    } else if ("USD".equals(compra.getMoneda())) {
                        montoNorm = montoNorm.divide(compra.getTipoCambio(), 2, RoundingMode.HALF_UP); // Sol a Dólar
                    }
                }
                totalPagadoNormalizado = totalPagadoNormalizado.add(montoNorm);
                compra.agregarPago(pago);
            }
        }
        compra.setMontoPagadoInicial(totalPagadoNormalizado);

        // Calcular Saldo
        BigDecimal saldo = compra.getTotal().subtract(totalPagadoNormalizado);
        compra.setSaldoPendiente(saldo.max(BigDecimal.ZERO));
        compra.setEstado((saldo.compareTo(BigDecimal.ZERO) <= 0) ? EstadoCompra.COMPLETADA : EstadoCompra.REGISTRADA);

        Compra savedCompra = compraRepository.save(compra);

        // 5. DETALLES DE PRODUCTOS
        if (request.getDetalles() != null) {
            for (CompraRequestDTO.DetalleRequestDTO detReq : request.getDetalles()) {
                Producto producto = productoRepository.findById(detReq.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                CompraDetalle detalle = new CompraDetalle();
                detalle.setCompra(savedCompra);
                detalle.setProducto(producto);
                detalle.setCantidad(detReq.getCantidad());
                detalle.setPrecioUnitario(detReq.getPrecioUnitario());
                detalle.calcularImporte();

                if (detReq.getAlmacenId() != null) {
                    detalle.setAlmacen(almacenRepository.findById(detReq.getAlmacenId().longValue()).orElse(null));
                }

                // Actualizar Stock solo si es Bien
                if(compra.getTipoCompra() == TipoCompra.BIEN) {
                    int stock = producto.getStockActual() != null ? producto.getStockActual() : 0;
                    producto.setStockActual(stock + detReq.getCantidad());

                    // Actualizar referencia de costo
                    if(savedCompra.getCodImportacion() != null) {
                        producto.setPrecioChina(detReq.getPrecioUnitario()); // Referencia Importación
                    } else {
                        producto.setCostoTotal(detReq.getPrecioUnitario()); // Referencia Local
                    }
                    productoRepository.save(producto);
                }
                detalleRepository.save(detalle);
            }
        }

        // ✅ 6. ACTUALIZAR TOTALES DE LA CARPETA (FOB, PESO, CBM)
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
        pago.setMoneda(compra.getMoneda()); // Asumimos misma moneda por ahora
        pago.setMetodoPago(metodo);
        pago.setFechaPago(LocalDateTime.now());
        pago.setReferencia(referencia);

        if (cuentaId != null) {
            pago.setCuentaOrigen(cuentaRepository.findById(cuentaId).orElse(null));
        }

        compra.agregarPago(pago);

        // Recalcular saldo
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

        // Revertir Stock
        if (compra.getTipoCompra() == TipoCompra.BIEN) {
            for (CompraDetalle d : compra.getDetalles()) {
                Producto p = d.getProducto();
                p.setStockActual(Math.max(0, p.getStockActual() - d.getCantidad()));
                productoRepository.save(p);
            }
        }

        compra.setEstado(EstadoCompra.ANULADA);
        compraRepository.save(compra);

        // ✅ Al anular, descontar de los totales de importación
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
        // Implementar búsqueda en repositorio si es necesario
        // Por ahora retornamos vacío o puedes usar un método custom en repo
        return Collections.emptyList();
    }

    @Override
    public List<CompraResponseDTO> listarPorProveedor(Integer proveedorId) {
        return compraRepository.findAll().stream() // Optimizar con findByProveedorId en repo
                .filter(c -> c.getProveedor().getId().equals(proveedorId))
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CompraResponseDTO> listarPorCodigoImportacion(String codImportacion) {
        return compraRepository.findByCodImportacion(codImportacion).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // =========================================================
    // ✅ MÉTODOS AUXILIARES
    // =========================================================

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
        dto.setTipoCompra(c.getTipoCompra() != null ? c.getTipoCompra().name() : "BIEN");
        dto.setTipoPago(c.getTipoPago() != null ? c.getTipoPago().name() : "CONTADO");
        dto.setFechaEmision(c.getFechaEmision());
        dto.setEstado(c.getEstado() != null ? c.getEstado().name() : "PENDIENTE");

        if (c.getProveedor() != null) {
            dto.setNombreProveedor(c.getProveedor().getNombre());
            dto.setRucProveedor(c.getProveedor().getRuc());
        }

        dto.setMoneda(c.getMoneda());
        dto.setTipoCambio(c.getTipoCambio());
        dto.setSubTotal(c.getSubTotal());
        dto.setFob(orZero(c.getFob()));
        dto.setIgv(orZero(c.getIgv()));
        dto.setTotal(c.getTotal());
        dto.setSaldoPendiente(c.getSaldoPendiente());

        // Datos Importación
        dto.setCodImportacion(c.getCodImportacion());
        dto.setPesoNetoKg(orZero(c.getPesoNetoKg()));
        dto.setCbm(orZero(c.getCbm()));

        // Resultados Prorrateo (Mapeo completo)
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

        // Mapeo de detalles y pagos (Simplificado para la lista)
        // Si necesitas detalles completos en la lista, descomenta o usa otro DTO
        return dto;
    }

    private BigDecimal orZero(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}