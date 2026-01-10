package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.request.ProductoRequestDTO;
import com.upc.smaf.dtos.response.ProductoResponseDTO  ;
import java.util.List;

public interface ProductoService {

    // ========== CRUD BÁSICO ==========

    /**
     * Crear un nuevo producto
     * @param request Datos del producto a crear
     * @return Producto creado con información completa
     * @throws RuntimeException Si la categoría no existe o el código ya existe
     */
    ProductoResponseDTO crearProducto(ProductoRequestDTO request);

    /**
     * Obtener producto por ID
     * @param id ID del producto
     * @return Producto encontrado
     * @throws RuntimeException Si el producto no existe
     */
    ProductoResponseDTO obtenerProducto(Integer id);

    /**
     * Listar TODOS los productos (activos e inactivos)
     * @return Lista de todos los productos
     */
    List<ProductoResponseDTO> listarProductos();

    /**
     * Listar solo productos ACTIVOS
     * @return Lista de productos activos
     */
    List<ProductoResponseDTO> listarProductosActivos();

    /**
     * Actualizar producto existente
     * @param id ID del producto a actualizar
     * @param request Nuevos datos del producto
     * @return Producto actualizado
     * @throws RuntimeException Si el producto o categoría no existen, o código duplicado
     */
    ProductoResponseDTO actualizarProducto(Integer id, ProductoRequestDTO request);

    /**
     * Desactivar producto (eliminación lógica)
     * @param id ID del producto a desactivar
     * @throws RuntimeException Si el producto no existe
     */
    void desactivarProducto(Integer id);



    List<ProductoResponseDTO> obtenerProductosConStockBajo();


    // ========== BÚSQUEDAS ==========

    /**
     * Buscar producto por código único
     * @param codigo Código del producto
     * @return Producto encontrado
     * @throws RuntimeException Si el producto no existe
     */
    ProductoResponseDTO obtenerProductoPorCodigo(String codigo);

    /**
     * Buscar productos por nombre (búsqueda parcial, case insensitive)
     * @param nombre Parte del nombre a buscar
     * @return Lista de productos que coinciden
     */
    List<ProductoResponseDTO> buscarProductosPorNombre(String nombre);


    // ========== CONSULTAS ESPECÍFICAS ==========

    /**
     * Verificar si un producto necesita reorden (stock < stock mínimo)
     * @param idProducto ID del producto
     * @return true si necesita reorden, false si no
     * @throws RuntimeException Si el producto no existe
     */
    Boolean necesitaReorden(Integer idProducto);

    /**
     * Obtener estado de stock de un producto
     * @param idProducto ID del producto
     * @return "AGOTADO", "BAJO", "NORMAL" o "ALTO"
     * @throws RuntimeException Si el producto no existe
     */
    String obtenerEstadoStock(Integer idProducto);


    // ========== MÉTODOS PARA FILTRADO (opcionales) ==========

    /**
     * Listar productos por categoría
     * @param idCategoria ID de la categoría
     * @return Lista de productos de esa categoría
     */
    // List<ProductoResponseDTO> listarProductosPorCategoria(Integer idCategoria);

    /**
     * Listar productos por rango de precios
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @return Lista de productos en ese rango de precios
     */
    // List<ProductoResponseDTO> listarProductosPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax);

    /**
     * Obtener productos ordenados por algún criterio
     * @param criterio "nombre", "stock", "precio", "fecha"
     * @param orden "asc" o "desc"
     * @return Lista de productos ordenada
     */
    // List<ProductoResponseDTO> listarProductosOrdenados(String criterio, String orden);
}