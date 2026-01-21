package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.ProductoAlmacenRequestDTO; // 👈 ASEGÚRATE DE IMPORTAR ESTO
import com.upc.smaf.dtos.request.ProductoRequestDTO;
import com.upc.smaf.dtos.response.ProductoResponseDTO;
import com.upc.smaf.entities.ProductoAlmacen; // 👈 Para devolver el movimiento (o usa un DTO)
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

    // ==========================================
    // 1. CREACIÓN (SOLO FICHA TÉCNICA)
    // ==========================================
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crearProducto(
            @Valid @RequestBody ProductoRequestDTO request) {
        try {
            // Este método ya no pide stock, crea el producto con stock 0
            ProductoResponseDTO response = productoService.crearProducto(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null); // O manejar mejor el error
        }
    }

    // ==========================================
    // 2. NUEVO: INGRESO DE STOCK (LOGÍSTICA)
    // ==========================================
    /**
     * Este endpoint se usa cuando llega el camión con mercadería.
     * Recibe: idProducto, idAlmacen, cantidad.
     */
    @PostMapping("/ingreso-stock")
    public ResponseEntity<?> ingresarStock(
            @Valid @RequestBody ProductoAlmacenRequestDTO request) {
        try {
            // Ejecutamos la lógica (esto funciona bien)
            productoService.agregarStock(request);

            // ✅ CAMBIO CLAVE: En lugar de devolver el objeto 'movimiento' (que tiene proxies),
            // devolvemos un mapa simple o un mensaje. Jackson será feliz con esto.
            return ResponseEntity.ok(java.util.Collections.singletonMap("mensaje", "Stock ingresado correctamente"));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==========================================
    // CRUD BÁSICO Y CONSULTAS (Se mantienen igual)
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerProducto(@PathVariable Integer id) {
        try {
            ProductoResponseDTO response = productoService.obtenerProducto(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listarProductos() {
        return ResponseEntity.ok(productoService.listarProductos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<ProductoResponseDTO>> listarProductosActivos() {
        return ResponseEntity.ok(productoService.listarProductosActivos());
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarProducto(@PathVariable Integer id) {
        try {
            productoService.desactivarProducto(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<List<ProductoResponseDTO>> obtenerProductosConStockBajo() {
        return ResponseEntity.ok(productoService.obtenerProductosConStockBajo());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoResponseDTO>> buscarProductosPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(productoService.buscarProductosPorNombre(nombre));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<ProductoResponseDTO> obtenerProductoPorCodigo(@PathVariable String codigo) {
        try {
            return ResponseEntity.ok(productoService.obtenerProductoPorCodigo(codigo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}