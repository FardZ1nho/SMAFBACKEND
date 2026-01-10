package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.ProductoRequestDTO;
import com.upc.smaf.dtos.response.ProductoResponseDTO;
import com.upc.smaf.serviceinterface.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // ========== ENDPOINTS CRUD ==========

    /**
     * Crear un nuevo producto
     * POST /api/productos
     */
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crearProducto(
            @Valid @RequestBody ProductoRequestDTO request) {
        try {
            ProductoResponseDTO response = productoService.crearProducto(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Obtener producto por ID
     * GET /api/productos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerProducto(@PathVariable Integer id) {
        try {
            ProductoResponseDTO response = productoService.obtenerProducto(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Listar todos los productos
     * GET /api/productos
     */
    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listarProductos() {
        List<ProductoResponseDTO> productos = productoService.listarProductos();
        return ResponseEntity.ok(productos);
    }

    /**
     * Listar solo productos activos
     * GET /api/productos/activos
     */
    @GetMapping("/activos")
    public ResponseEntity<List<ProductoResponseDTO>> listarProductosActivos() {
        List<ProductoResponseDTO> productos = productoService.listarProductosActivos();
        return ResponseEntity.ok(productos);
    }

    /**
     * Actualizar producto
     * PUT /api/productos/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizarProducto(
            @PathVariable Integer id,
            @Valid @RequestBody ProductoRequestDTO request) {
        try {
            ProductoResponseDTO response = productoService.actualizarProducto(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Desactivar producto (eliminación lógica)
     * DELETE /api/productos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarProducto(@PathVariable Integer id) {
        try {
            productoService.desactivarProducto(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== ENDPOINTS DE STOCK ==========


    /**
     * Obtener productos con stock bajo
     * GET /api/productos/stock-bajo
     */
    @GetMapping("/stock-bajo")
    public ResponseEntity<List<ProductoResponseDTO>> obtenerProductosConStockBajo() {
        List<ProductoResponseDTO> productos = productoService.obtenerProductosConStockBajo();
        return ResponseEntity.ok(productos);
    }

    // ========== ENDPOINTS DE BÚSQUEDA ==========

    /**
     * Buscar producto por código
     * GET /api/productos/codigo/{codigo}
     */
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<ProductoResponseDTO> obtenerProductoPorCodigo(
            @PathVariable String codigo) {
        try {
            ProductoResponseDTO response = productoService.obtenerProductoPorCodigo(codigo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Buscar productos por nombre
     * GET /api/productos/buscar?nombre=martillo
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoResponseDTO>> buscarProductosPorNombre(
            @RequestParam String nombre) {
        List<ProductoResponseDTO> productos = productoService.buscarProductosPorNombre(nombre);
        return ResponseEntity.ok(productos);
    }

    // ========== ENDPOINTS DE CONSULTA ==========

    /**
     * Verificar si un producto necesita reorden
     * GET /api/productos/{id}/necesita-reorden
     */
    @GetMapping("/{id}/necesita-reorden")
    public ResponseEntity<Boolean> necesitaReorden(@PathVariable Integer id) {
        try {
            Boolean necesita = productoService.necesitaReorden(id);
            return ResponseEntity.ok(necesita);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtener estado de stock de un producto
     * GET /api/productos/{id}/estado-stock
     */
    @GetMapping("/{id}/estado-stock")
    public ResponseEntity<String> obtenerEstadoStock(@PathVariable Integer id) {
        try {
            String estado = productoService.obtenerEstadoStock(id);
            return ResponseEntity.ok(estado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== ENDPOINTS ADICIONALES ==========

    /**
     * Endpoint de salud (para probar que el controller funciona)
     * GET /api/productos/salud
     */
    @GetMapping("/salud")
    public ResponseEntity<String> salud() {
        return ResponseEntity.ok("✅ Módulo Productos funcionando correctamente");
    }

    /**
     * Listar productos por categoría (EXTRA)
     * GET /api/productos/categoria/{idCategoria}
     */
    /*
    @GetMapping("/categoria/{idCategoria}")
    public ResponseEntity<List<ProductoResponseDTO>> listarProductosPorCategoria(
            @PathVariable Integer idCategoria) {
        try {
            List<ProductoResponseDTO> productos = productoService.listarProductosPorCategoria(idCategoria);
            return ResponseEntity.ok(productos);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    */

    /**
     * Reactivar producto desactivado (EXTRA)
     * PATCH /api/productos/{id}/reactivar
     */
    /*
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ProductoResponseDTO> reactivarProducto(@PathVariable Integer id) {
        try {
            ProductoResponseDTO response = productoService.reactivarProducto(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    */
}