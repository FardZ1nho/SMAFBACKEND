package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.request.ProductoRequestDTO;
import com.upc.smaf.dtos.request.ProductoAlmacenRequestDTO;
import com.upc.smaf.dtos.response.ProductoResponseDTO;
import com.upc.smaf.entities.ProductoAlmacen;
import java.util.List;

public interface ProductoService {

    // ========== CRUD BÁSICO ==========
    ProductoResponseDTO crearProducto(ProductoRequestDTO request);

    ProductoResponseDTO obtenerProducto(Integer id);

    List<ProductoResponseDTO> listarProductos();

    List<ProductoResponseDTO> listarProductosActivos();

    ProductoResponseDTO actualizarProducto(Integer id, ProductoRequestDTO request);

    void desactivarProducto(Integer id);

    List<ProductoResponseDTO> obtenerProductosConStockBajo();

    // ========== BÚSQUEDAS ==========
    ProductoResponseDTO obtenerProductoPorCodigo(String codigo);

    List<ProductoResponseDTO> buscarProductosPorNombre(String nombre);

    // ========== CONSULTAS ESPECÍFICAS ==========
    Boolean necesitaReorden(Integer idProducto);

    String obtenerEstadoStock(Integer idProducto);

    ProductoAlmacen agregarStock(ProductoAlmacenRequestDTO request);

    void reducirStock(Integer idProducto, int cantidadVenta);

    // ✅ NUEVO: Método para cuadrar la base de datos con la realidad física
    void sincronizarStockReal();
}