package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.ProductoAlmacenRequestDTO;
import com.upc.smaf.dtos.response.ProductoAlmacenResponseDTO;
import com.upc.smaf.serviceinterface.ProductoAlmacenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/producto-almacen")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProductoAlmacenController {

    private final ProductoAlmacenService productoAlmacenService;

    /**
     * Asignar un producto a un almacén
     * POST /producto-almacen
     */
    @PostMapping
    public ResponseEntity<ProductoAlmacenResponseDTO> asignarProductoAAlmacen(
            @Valid @RequestBody ProductoAlmacenRequestDTO request) {
        ProductoAlmacenResponseDTO response = productoAlmacenService.asignarProductoAAlmacen(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Actualizar información de un producto en un almacén
     * PUT /producto-almacen/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductoAlmacenResponseDTO> actualizarProductoAlmacen(
            @PathVariable Long id,
            @Valid @RequestBody ProductoAlmacenRequestDTO request) {
        ProductoAlmacenResponseDTO response = productoAlmacenService.actualizarProductoAlmacen(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener información específica por ID
     * GET /producto-almacen/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoAlmacenResponseDTO> obtenerPorId(@PathVariable Long id) {
        ProductoAlmacenResponseDTO response = productoAlmacenService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar todas las ubicaciones de un producto
     * GET /producto-almacen/producto/{productoId}
     */
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<ProductoAlmacenResponseDTO>> listarUbicacionesPorProducto(
            @PathVariable Integer productoId) {
        List<ProductoAlmacenResponseDTO> ubicaciones =
                productoAlmacenService.listarUbicacionesPorProducto(productoId);
        return ResponseEntity.ok(ubicaciones);
    }

    /**
     * Listar todos los productos en un almacén
     * GET /producto-almacen/almacen/{almacenId}
     */
    @GetMapping("/almacen/{almacenId}")
    public ResponseEntity<List<ProductoAlmacenResponseDTO>> listarProductosPorAlmacen(
            @PathVariable Long almacenId) {
        List<ProductoAlmacenResponseDTO> productos =
                productoAlmacenService.listarProductosPorAlmacen(almacenId);
        return ResponseEntity.ok(productos);
    }

    /**
     * Obtener stock de un producto en un almacén específico
     * GET /producto-almacen/stock?productoId=1&almacenId=2
     */
    @GetMapping("/stock")
    public ResponseEntity<ProductoAlmacenResponseDTO> obtenerStockEnAlmacen(
            @RequestParam Integer productoId,
            @RequestParam Long almacenId) {
        ProductoAlmacenResponseDTO response =
                productoAlmacenService.obtenerStockEnAlmacen(productoId, almacenId);
        return ResponseEntity.ok(response);
    }

    /**
     * Transferir stock entre almacenes
     * POST /producto-almacen/transferir
     */
    @PostMapping("/transferir")
    public ResponseEntity<Void> transferirStockEntreAlmacenes(
            @RequestParam Integer productoId,
            @RequestParam Long almacenOrigenId,
            @RequestParam Long almacenDestinoId,
            @RequestParam Integer cantidad) {
        productoAlmacenService.transferirStockEntreAlmacenes(
                productoId, almacenOrigenId, almacenDestinoId, cantidad);
        return ResponseEntity.ok().build();
    }

    /**
     * Ajustar stock en un almacén (incrementar o decrementar)
     * PATCH /producto-almacen/{id}/ajustar-stock?cantidad=10&motivo=Ajuste+manual
     */
    @PatchMapping("/{id}/ajustar-stock")
    public ResponseEntity<ProductoAlmacenResponseDTO> ajustarStock(
            @PathVariable Long id,
            @RequestParam Integer cantidad,
            @RequestParam(required = false) String motivo) {
        ProductoAlmacenResponseDTO response =
                productoAlmacenService.ajustarStock(id, cantidad, motivo);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar asignación de producto en almacén
     * DELETE /producto-almacen/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProductoDeAlmacen(@PathVariable Long id) {
        productoAlmacenService.eliminarProductoDeAlmacen(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Calcular stock total de un producto (suma de todos los almacenes)
     * GET /producto-almacen/producto/{productoId}/stock-total
     */
    @GetMapping("/producto/{productoId}/stock-total")
    public ResponseEntity<Integer> calcularStockTotalProducto(@PathVariable Integer productoId) {
        Integer stockTotal = productoAlmacenService.calcularStockTotalProducto(productoId);
        return ResponseEntity.ok(stockTotal);
    }
}