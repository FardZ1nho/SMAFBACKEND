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

    @Override
    @Transactional
    public CompraResponseDTO registrarCompra(CompraRequestDTO request) {
        // 1. Validar Proveedor
        Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        // 2. Crear Cabecera (Compra)
        Compra compra = new Compra();
        compra.setTipoComprobante(request.getTipoComprobante());
        compra.setSerie(request.getSerie());
        compra.setNumero(request.getNumero());
        compra.setFecEmision(request.getFecEmision());
        compra.setProveedor(proveedor);
        compra.setMoneda(request.getMoneda());
        compra.setTipoCambio(request.getTipoCambio());
        compra.setObservaciones(request.getObservaciones());

        // Guardamos primero la cabecera para tener el ID
        Compra savedCompra = compraRepository.save(compra);

        // 3. Procesar los Detalles
        if (request.getDetalles() != null) {
            for (CompraRequestDTO.DetalleRequestDTO detReq : request.getDetalles()) {

                // Buscar Producto
                Producto producto = productoRepository.findById(detReq.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado ID: " + detReq.getProductoId()));

                // Buscar Almacén (Convirtiendo Integer a Long)
                Almacen almacen = almacenRepository.findById(detReq.getAlmacenId().longValue())
                        .orElseThrow(() -> new RuntimeException("Almacén no encontrado ID: " + detReq.getAlmacenId()));

                // Crear Entidad Detalle
                CompraDetalle detalle = new CompraDetalle();
                detalle.setCompra(savedCompra); // IMPORTANTE: Enlace con el padre
                detalle.setProducto(producto);
                detalle.setAlmacen(almacen);
                detalle.setCantidad(detReq.getCantidad());
                detalle.setPrecioUnitario(detReq.getPrecioUnitario());

                detalleRepository.save(detalle);

                // 4. Actualizar Stock del producto
                productoRepository.aumentarStock(producto.getId(), detReq.getCantidad());
            }
        }

        // Retornamos la compra completa mapeada
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

    // --- MÉTODO DE MAPEO (AQUÍ ESTABA EL ERROR) ---
    private CompraResponseDTO mapToResponseDTO(Compra compra) {
        CompraResponseDTO res = new CompraResponseDTO();
        res.setId(compra.getId());
        res.setTipoComprobante(compra.getTipoComprobante());
        res.setSerie(compra.getSerie());
        res.setNumero(compra.getNumero());
        res.setFecEmision(compra.getFecEmision());

        // 🔥 CÁLCULO DEL TOTAL 🔥
        double totalCalculado = 0.0;
        if (compra.getDetalles() != null) {
            totalCalculado = compra.getDetalles().stream()
                    .mapToDouble(d -> d.getCantidad() * d.getPrecioUnitario())
                    .sum();
        }

        // 👇 ESTA ES LA LÍNEA QUE FALTABA PARA QUE SE VEA EL MONTO 👇
        res.setTotal(totalCalculado);
        // -----------------------------------------------------------

        if (compra.getProveedor() != null) {
            res.setNombreProveedor(compra.getProveedor().getNombre());
            res.setRucProveedor(compra.getProveedor().getRuc());
        }

        res.setMoneda(compra.getMoneda());
        res.setTipoCambio(compra.getTipoCambio());
        res.setObservaciones(compra.getObservaciones());
        res.setFechaRegistro(compra.getFechaRegistro());

        // Mapeo de la lista de detalles para el Frontend
        if (compra.getDetalles() != null) {
            List<CompraDetalleResponseDTO> detallesDTO = compra.getDetalles().stream()
                    .map(det -> {
                        CompraDetalleResponseDTO d = new CompraDetalleResponseDTO();
                        // Datos del producto
                        d.setProductoId(det.getProducto().getId());
                        d.setNombreProducto(det.getProducto().getNombre());
                        d.setCodigoProducto(det.getProducto().getCodigo());

                        // Datos de la transacción
                        d.setCantidad(det.getCantidad());
                        d.setPrecioUnitario(det.getPrecioUnitario());
                        return d;
                    }).collect(Collectors.toList());

            res.setDetalles(detallesDTO);
        }

        return res;
    }
}