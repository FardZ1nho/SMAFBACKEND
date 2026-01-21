package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.CompraRequestDTO;
import com.upc.smaf.dtos.response.CompraDetalleResponseDTO;
import com.upc.smaf.dtos.response.CompraResponseDTO;
import com.upc.smaf.entities.*;
import com.upc.smaf.entities.TipoCompra;
import com.upc.smaf.entities.TipoComprobante;
import com.upc.smaf.repositories.*;
import com.upc.smaf.serviceinterface.CompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    // ✅ 1. INYECCIÓN DEL REPOSITORIO DE IMPORTACIONES
    private final ImportacionRepository importacionRepository;

    @Override
    @Transactional
    public CompraResponseDTO registrarCompra(CompraRequestDTO request) {
        // 1. Validar Proveedor
        Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        // 2. Convertir Enums (Validación estricta)
        TipoCompra tipoCompra;
        try {
            tipoCompra = TipoCompra.valueOf(request.getTipoCompra());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tipo de compra inválido (Debe ser BIEN o SERVICIO)");
        }

        TipoComprobante tipoComprobante;
        try {
            tipoComprobante = TipoComprobante.valueOf(request.getTipoComprobante());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tipo de comprobante inválido");
        }

        // 3. Crear Cabecera (Compra)
        Compra compra = new Compra();
        compra.setTipoCompra(tipoCompra);
        compra.setTipoComprobante(tipoComprobante);
        compra.setSerie(request.getSerie());
        compra.setNumero(request.getNumero());
        compra.setFechaEmision(request.getFechaEmision());
        compra.setFechaVencimiento(request.getFechaVencimiento());
        compra.setProveedor(proveedor);
        compra.setMoneda(request.getMoneda());
        compra.setTipoCambio(request.getTipoCambio());
        compra.setObservaciones(request.getObservaciones());

        // MONTOS E IMPUESTOS
        compra.setSubTotal(request.getSubTotal());
        compra.setIgv(request.getIgv());
        compra.setTotal(request.getTotal());

        compra.setPercepcion(request.getPercepcion());
        compra.setDetraccionPorcentaje(request.getDetraccionPorcentaje());
        compra.setDetraccionMonto(request.getDetraccionMonto());
        compra.setRetencion(request.getRetencion());

        // Guardamos cabecera primero para obtener el ID
        Compra savedCompra = compraRepository.save(compra);

        // ====================================================
        // ✅ 2. LÓGICA AUTOMÁTICA DE IMPORTACIÓN
        // ====================================================
        if (savedCompra.getTipoComprobante() == TipoComprobante.FACTURA_COMERCIAL) {
            Importacion imp = new Importacion();
            imp.setCompra(savedCompra); // Relacionamos con la compra recién creada
            imp.setEstado(EstadoImportacion.ORDENADO); // Estado inicial

            // Inicializamos costos en 0 para evitar nulos
            imp.setCostoFlete(BigDecimal.ZERO);
            imp.setCostoSeguro(BigDecimal.ZERO);
            imp.setImpuestosAduanas(BigDecimal.ZERO);
            imp.setGastosOperativos(BigDecimal.ZERO); // O setOtrosGastos según tu entidad final

            importacionRepository.save(imp);
        }
        // ====================================================

        // 4. Procesar Detalles
        if (request.getDetalles() != null) {
            for (CompraRequestDTO.DetalleRequestDTO detReq : request.getDetalles()) {

                // Buscar Producto
                Producto producto = productoRepository.findById(detReq.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado ID: " + detReq.getProductoId()));

                // Crear Entidad Detalle
                CompraDetalle detalle = new CompraDetalle();
                detalle.setCompra(savedCompra);
                detalle.setProducto(producto);
                detalle.setCantidad(detReq.getCantidad());
                detalle.setPrecioUnitario(detReq.getPrecioUnitario()); // Precio Base (Sin IGV)
                detalle.calcularImporte();

                // ====================================================
                // 🚀 LÓGICA SIMPLIFICADA (ACTUALIZACIÓN DE PRECIOS)
                // ====================================================
                if (tipoCompra == TipoCompra.BIEN) {

                    // A. Validar Almacén
                    if (detReq.getAlmacenId() == null) {
                        throw new RuntimeException("Para compra de BIENES, el almacén es obligatorio.");
                    }
                    Almacen almacen = almacenRepository.findById(detReq.getAlmacenId().longValue())
                            .orElseThrow(() -> new RuntimeException("Almacén no encontrado"));
                    detalle.setAlmacen(almacen);

                    // B. Actualizar Stock Físico (Suma simple)
                    int stockActual = producto.getStockActual() != null ? producto.getStockActual() : 0;
                    int nuevoStock = stockActual + detReq.getCantidad().intValue();
                    producto.setStockActual(nuevoStock);

                    // C. ACTUALIZACIÓN DE PRECIOS DIRECTA (Sin Promedios)
                    BigDecimal precioBaseCompra = detReq.getPrecioUnitario();
                    BigDecimal factorIGV = new BigDecimal("1.18");

                    // Calculamos: Precio Base * 1.18
                    BigDecimal costoConIGV = precioBaseCompra.multiply(factorIGV).setScale(2, RoundingMode.HALF_UP);

                    // -> SOBRESCRIBIMOS EL COSTO TOTAL CON EL ÚLTIMO PRECIO (CON IGV)
                    producto.setCostoTotal(costoConIGV);

                    // -> También actualizamos el precio china (referencia base sin IGV)
                    producto.setPrecioChina(precioBaseCompra);

                    productoRepository.save(producto);

                } else {
                    // SI ES SERVICIO
                    detalle.setAlmacen(null);
                }
                // ====================================================

                detalleRepository.save(detalle);
            }
        }

        return obtenerCompra(savedCompra.getId());
    }

    @Override
    @Transactional(readOnly = true)
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
    public List<CompraResponseDTO> listarPorProveedor(Integer proveedorId) {
        return compraRepository.findByProveedorId(proveedorId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CompraResponseDTO> buscarPorNumero(String numero) {
        return compraRepository.buscarPorNumero(numero).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // --- MAPEO DE ENTIDAD A DTO ---
    private CompraResponseDTO mapToResponseDTO(Compra compra) {
        CompraResponseDTO res = new CompraResponseDTO();
        res.setId(compra.getId());

        res.setTipoCompra(compra.getTipoCompra().name());
        res.setTipoComprobante(compra.getTipoComprobante().name());

        res.setSerie(compra.getSerie());
        res.setNumero(compra.getNumero());
        res.setFechaEmision(compra.getFechaEmision());
        res.setFechaVencimiento(compra.getFechaVencimiento());
        res.setFechaRegistro(compra.getFechaRegistro());

        if (compra.getProveedor() != null) {
            res.setNombreProveedor(compra.getProveedor().getNombre());
            res.setRucProveedor(compra.getProveedor().getRuc());
        }

        res.setMoneda(compra.getMoneda());
        res.setTipoCambio(compra.getTipoCambio());
        res.setObservaciones(compra.getObservaciones());

        res.setSubTotal(compra.getSubTotal());
        res.setIgv(compra.getIgv());
        res.setTotal(compra.getTotal());

        res.setPercepcion(compra.getPercepcion());
        res.setDetraccionPorcentaje(compra.getDetraccionPorcentaje());
        res.setDetraccionMonto(compra.getDetraccionMonto());
        res.setRetencion(compra.getRetencion());

        if (compra.getDetalles() != null) {
            List<CompraDetalleResponseDTO> detallesDTO = compra.getDetalles().stream()
                    .map(det -> {
                        CompraDetalleResponseDTO d = new CompraDetalleResponseDTO();
                        d.setId(det.getId());

                        if (det.getProducto() != null) {
                            d.setProductoId(det.getProducto().getId());
                            d.setNombreProducto(det.getProducto().getNombre());
                            d.setCodigoProducto(det.getProducto().getCodigo());
                        }

                        if (det.getAlmacen() != null) {
                            d.setAlmacenId(det.getAlmacen().getId().intValue());
                            d.setNombreAlmacen(det.getAlmacen().getNombre());
                        }

                        d.setCantidad(det.getCantidad());
                        d.setPrecioUnitario(det.getPrecioUnitario());
                        d.setImporteTotal(det.getImporteTotal());

                        return d;
                    }).collect(Collectors.toList());

            res.setDetalles(detallesDTO);
        }

        return res;
    }
}