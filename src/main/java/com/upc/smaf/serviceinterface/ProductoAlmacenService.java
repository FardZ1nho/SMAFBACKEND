package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.request.ProductoAlmacenRequestDTO;
import com.upc.smaf.dtos.response.ProductoAlmacenResponseDTO;

import java.util.List;

public interface ProductoAlmacenService {

    // Asignar un producto a un almacén (o actualizar stock si ya existe)
    ProductoAlmacenResponseDTO asignarProductoAAlmacen(ProductoAlmacenRequestDTO request);

    // Actualizar información de un producto en un almacén
    ProductoAlmacenResponseDTO actualizarProductoAlmacen(Long id, ProductoAlmacenRequestDTO request);

    // Obtener información específica por ID
    ProductoAlmacenResponseDTO obtenerPorId(Long id);

    // Listar todas las ubicaciones de un producto
    List<ProductoAlmacenResponseDTO> listarUbicacionesPorProducto(Integer productoId);

    // Listar todos los productos en un almacén
    List<ProductoAlmacenResponseDTO> listarProductosPorAlmacen(Long almacenId);

    // Obtener stock de un producto en un almacén específico
    ProductoAlmacenResponseDTO obtenerStockEnAlmacen(Integer productoId, Long almacenId);

    // Transferir stock entre almacenes
    void transferirStockEntreAlmacenes(Integer productoId, Long almacenOrigenId,
                                       Long almacenDestinoId, Integer cantidad);

    // Ajustar stock en un almacén (incrementar o decrementar)
    ProductoAlmacenResponseDTO ajustarStock(Long id, Integer cantidad, String motivo);

    // Eliminar asignación de producto en almacén
    void eliminarProductoDeAlmacen(Long id);

    // Calcular stock total de un producto (suma de todos los almacenes)
    Integer calcularStockTotalProducto(Integer productoId);
}