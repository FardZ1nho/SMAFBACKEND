package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.ProductoAlmacenRequestDTO;
import com.upc.smaf.dtos.request.ProductoRequestDTO;
import com.upc.smaf.dtos.response.ProductoResponseDTO;
import com.upc.smaf.entities.ProductoAlmacen;
import com.upc.smaf.serviceinterface.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 👈 Agregado para evitar problemas de CORS con Angular
public class ProductoController {

    private final ProductoService productoService;

    // ==========================================
    // 1. CREACIÓN (SOLO FICHA TÉCNICA)
    // ==========================================
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

    // ==========================================
    // 2. INGRESO DE STOCK (LOGÍSTICA)
    // ==========================================
    @PostMapping("/ingreso-stock")
    public ResponseEntity<?> ingresarStock(
            @Valid @RequestBody ProductoAlmacenRequestDTO request) {
        try {
            productoService.agregarStock(request);
            return ResponseEntity.ok(java.util.Collections.singletonMap("mensaje", "Stock ingresado correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==========================================
    // ✅ 3. NUEVO: SINCRONIZADOR MAESTRO DE STOCK
    // ==========================================
    @PostMapping("/sincronizar-stock")
    public ResponseEntity<?> sincronizarStockReal() {
        try {
            productoService.sincronizarStockReal();
            return ResponseEntity.ok(Map.of("message", "Stock sincronizado correctamente con los almacenes reales."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ==========================================
    // CRUD BÁSICO Y CONSULTAS
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