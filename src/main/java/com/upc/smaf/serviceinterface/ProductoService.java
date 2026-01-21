package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.request.ProductoRequestDTO;
import com.upc.smaf.dtos.request.ProductoAlmacenRequestDTO; // 👈 1. IMPORTANTE: Importar el DTO
import com.upc.smaf.dtos.response.ProductoResponseDTO;
import com.upc.smaf.entities.ProductoAlmacen; // 👈 2. IMPORTANTE: Importar la Entidad de respuesta
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

    // 👇👇👇 3. AGREGA ESTA LÍNEA AL FINAL (Esto solucionará el error rojo) 👇👇👇
    ProductoAlmacen agregarStock(ProductoAlmacenRequestDTO request);
}