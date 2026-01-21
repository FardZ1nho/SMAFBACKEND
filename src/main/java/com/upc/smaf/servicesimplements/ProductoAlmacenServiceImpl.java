package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.ProductoAlmacenRequestDTO;
import com.upc.smaf.dtos.response.ProductoAlmacenResponseDTO;
import com.upc.smaf.entities.Almacen;
import com.upc.smaf.entities.Producto;
import com.upc.smaf.entities.ProductoAlmacen;
import com.upc.smaf.repositories.AlmacenRepository;
import com.upc.smaf.repositories.ProductoAlmacenRepository;
import com.upc.smaf.repositories.ProductoRepository;
import com.upc.smaf.serviceinterface.ProductoAlmacenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoAlmacenServiceImpl implements ProductoAlmacenService {

    private final ProductoAlmacenRepository productoAlmacenRepository;
    private final ProductoRepository productoRepository;
    private final AlmacenRepository almacenRepository;

    @Override
    @Transactional
    public ProductoAlmacenResponseDTO asignarProductoAAlmacen(ProductoAlmacenRequestDTO request) {
        // Validar que el producto existe
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + request.getProductoId()));

        // Validar que el almacén existe
        Almacen almacen = almacenRepository.findById(request.getAlmacenId())
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado con ID: " + request.getAlmacenId()));

        // Verificar si ya existe la asignación
        ProductoAlmacen productoAlmacen = productoAlmacenRepository
                .findByProductoIdAndAlmacenId(request.getProductoId(), request.getAlmacenId())
                .orElse(new ProductoAlmacen());

        productoAlmacen.setProducto(producto);
        productoAlmacen.setAlmacen(almacen);
        productoAlmacen.setStock(request.getStockMinimo());
        productoAlmacen.setUbicacionFisica(request.getUbicacionFisica());
        productoAlmacen.setStockMinimo(request.getStockMinimo());
        productoAlmacen.setActivo(request.getActivo() != null ? request.getActivo() : true);

        ProductoAlmacen guardado = productoAlmacenRepository.save(productoAlmacen);

        // Actualizar stock total del producto
        actualizarStockTotalProducto(producto);

        return convertirAResponseDTO(guardado);
    }

    @Override
    @Transactional
    public ProductoAlmacenResponseDTO actualizarProductoAlmacen(Long id, ProductoAlmacenRequestDTO request) {
        ProductoAlmacen productoAlmacen = productoAlmacenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada con ID: " + id));

        productoAlmacen.setStock(request.getStockMinimo());
        productoAlmacen.setUbicacionFisica(request.getUbicacionFisica());
        productoAlmacen.setStockMinimo(request.getStockMinimo());

        if (request.getActivo() != null) {
            productoAlmacen.setActivo(request.getActivo());
        }

        ProductoAlmacen actualizado = productoAlmacenRepository.save(productoAlmacen);

        // Actualizar stock total del producto
        actualizarStockTotalProducto(productoAlmacen.getProducto());

        return convertirAResponseDTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoAlmacenResponseDTO obtenerPorId(Long id) {
        ProductoAlmacen productoAlmacen = productoAlmacenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada con ID: " + id));
        return convertirAResponseDTO(productoAlmacen);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoAlmacenResponseDTO> listarUbicacionesPorProducto(Integer productoId) {
        return productoAlmacenRepository.findByProductoIdAndActivoTrue(productoId).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoAlmacenResponseDTO> listarProductosPorAlmacen(Long almacenId) {
        return productoAlmacenRepository.findByAlmacenIdAndActivoTrue(almacenId).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoAlmacenResponseDTO obtenerStockEnAlmacen(Integer productoId, Long almacenId) {
        ProductoAlmacen productoAlmacen = productoAlmacenRepository
                .findByProductoIdAndAlmacenId(productoId, almacenId)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró el producto " + productoId + " en el almacén " + almacenId));
        return convertirAResponseDTO(productoAlmacen);
    }

    @Override
    @Transactional
    public void transferirStockEntreAlmacenes(Integer productoId, Long almacenOrigenId,
                                              Long almacenDestinoId, Integer cantidad) {
        if (cantidad <= 0) {
            throw new RuntimeException("La cantidad a transferir debe ser mayor a 0");
        }

        // Obtener origen
        ProductoAlmacen origen = productoAlmacenRepository
                .findByProductoIdAndAlmacenId(productoId, almacenOrigenId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en almacén origen"));

        // Validar stock disponible
        if (origen.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente en almacén origen. Disponible: " + origen.getStock());
        }

        // Obtener o crear destino
        ProductoAlmacen destino = productoAlmacenRepository
                .findByProductoIdAndAlmacenId(productoId, almacenDestinoId)
                .orElseGet(() -> {
                    Almacen almacenDestino = almacenRepository.findById(almacenDestinoId)
                            .orElseThrow(() -> new RuntimeException("Almacén destino no encontrado"));
                    ProductoAlmacen nuevo = new ProductoAlmacen();
                    nuevo.setProducto(origen.getProducto());
                    nuevo.setAlmacen(almacenDestino);
                    nuevo.setStock(0);
                    nuevo.setActivo(true);
                    return nuevo;
                });

        // Realizar transferencia
        origen.setStock(origen.getStock() - cantidad);
        destino.setStock(destino.getStock() + cantidad);

        productoAlmacenRepository.save(origen);
        productoAlmacenRepository.save(destino);

        // Actualizar stock total (aunque debería ser el mismo)
        actualizarStockTotalProducto(origen.getProducto());
    }

    @Override
    @Transactional
    public ProductoAlmacenResponseDTO ajustarStock(Long id, Integer cantidad, String motivo) {
        ProductoAlmacen productoAlmacen = productoAlmacenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada con ID: " + id));

        int nuevoStock = productoAlmacen.getStock() + cantidad;

        if (nuevoStock < 0) {
            throw new RuntimeException("El stock no puede ser negativo. Stock actual: " + productoAlmacen.getStock());
        }

        productoAlmacen.setStock(nuevoStock);
        ProductoAlmacen actualizado = productoAlmacenRepository.save(productoAlmacen);

        // Actualizar stock total del producto
        actualizarStockTotalProducto(productoAlmacen.getProducto());

        // TODO: Registrar en tabla de movimientos el motivo del ajuste

        return convertirAResponseDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminarProductoDeAlmacen(Long id) {
        ProductoAlmacen productoAlmacen = productoAlmacenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada con ID: " + id));

        Producto producto = productoAlmacen.getProducto();
        productoAlmacenRepository.deleteById(id);

        // Actualizar stock total del producto
        actualizarStockTotalProducto(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer calcularStockTotalProducto(Integer productoId) {
        return productoAlmacenRepository.calcularStockTotalProducto(productoId);
    }

    // Método privado para actualizar el stock total del producto
    private void actualizarStockTotalProducto(Producto producto) {
        Integer stockTotal = productoAlmacenRepository.calcularStockTotalProducto(producto.getId());
        producto.setStockActual(stockTotal);
        productoRepository.save(producto);
    }

    // Método auxiliar para convertir entidad a DTO
    private ProductoAlmacenResponseDTO convertirAResponseDTO(ProductoAlmacen productoAlmacen) {
        ProductoAlmacenResponseDTO dto = new ProductoAlmacenResponseDTO();
        dto.setId(productoAlmacen.getId());

        // Datos del producto
        dto.setProductoId(productoAlmacen.getProducto().getId());
        dto.setProductoNombre(productoAlmacen.getProducto().getNombre());
        dto.setProductoCodigo(productoAlmacen.getProducto().getCodigo());

        // Datos del almacén
        dto.setAlmacenId(productoAlmacen.getAlmacen().getId());
        dto.setAlmacenCodigo(productoAlmacen.getAlmacen().getCodigo());
        dto.setAlmacenNombre(productoAlmacen.getAlmacen().getNombre());

        // Stock y ubicación
        dto.setStock(productoAlmacen.getStock());
        dto.setUbicacionFisica(productoAlmacen.getUbicacionFisica());
        dto.setStockMinimo(productoAlmacen.getStockMinimo());

        dto.setActivo(productoAlmacen.getActivo());
        dto.setFechaCreacion(productoAlmacen.getFechaCreacion());
        dto.setFechaActualizacion(productoAlmacen.getFechaActualizacion());

        return dto;
    }
}