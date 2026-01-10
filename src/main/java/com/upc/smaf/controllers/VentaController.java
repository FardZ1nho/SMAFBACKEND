package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.VentaRequestDTO;
import com.upc.smaf.dtos.response.VentaResponseDTO;
import com.upc.smaf.entities.EstadoVenta;
import com.upc.smaf.serviceinterface.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/ventas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VentaController {

    private final VentaService ventaService;

    // ========== CREAR VENTA COMPLETA ==========

    /**
     * Crear una nueva venta y completarla inmediatamente
     * POST /ventas
     */
    @PostMapping
    public ResponseEntity<VentaResponseDTO> crearVenta(@Valid @RequestBody VentaRequestDTO request) {
        try {
            VentaResponseDTO response = ventaService.crearVenta(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ========== GUARDAR BORRADOR ==========

    /**
     * Guardar una venta como borrador (sin descontar stock)
     * POST /ventas/borrador
     */
    @PostMapping("/borrador")
    public ResponseEntity<VentaResponseDTO> guardarBorrador(@Valid @RequestBody VentaRequestDTO request) {
        try {
            VentaResponseDTO response = ventaService.guardarBorrador(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ========== COMPLETAR VENTA DESDE BORRADOR ==========

    /**
     * Completar una venta que está en estado BORRADOR
     * POST /ventas/{id}/completar
     */
    @PostMapping("/{id}/completar")
    public ResponseEntity<VentaResponseDTO> completarVenta(@PathVariable Integer id) {
        try {
            VentaResponseDTO response = ventaService.completarVenta(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ========== CANCELAR VENTA ==========

    /**
     * Cancelar una venta y devolver el stock
     * POST /ventas/{id}/cancelar
     */
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarVenta(@PathVariable Integer id) {
        try {
            ventaService.cancelarVenta(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ========== OBTENER VENTA ==========

    /**
     * Obtener una venta por su ID
     * GET /ventas/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> obtenerVenta(@PathVariable Integer id) {
        try {
            VentaResponseDTO response = ventaService.obtenerVenta(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== BUSCAR POR CÓDIGO ==========

    /**
     * Buscar venta por código
     * GET /ventas/codigo/{codigo}
     */
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<VentaResponseDTO> buscarPorCodigo(@PathVariable String codigo) {
        try {
            VentaResponseDTO response = ventaService.buscarPorCodigo(codigo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== LISTAR TODAS ==========

    /**
     * Listar todas las ventas
     * GET /ventas
     */
    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> listarTodas() {
        List<VentaResponseDTO> ventas = ventaService.listarTodas();
        return ResponseEntity.ok(ventas);
    }

    // ========== LISTAR POR ESTADO ==========

    /**
     * Listar ventas por estado
     * GET /ventas/estado/{estado}
     * Ejemplo: /ventas/estado/COMPLETADA
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<VentaResponseDTO>> listarPorEstado(@PathVariable EstadoVenta estado) {
        List<VentaResponseDTO> ventas = ventaService.listarPorEstado(estado);
        return ResponseEntity.ok(ventas);
    }

    // ========== LISTAR BORRADORES ==========

    /**
     * Listar solo ventas en estado BORRADOR
     * GET /ventas/borradores
     */
    @GetMapping("/borradores")
    public ResponseEntity<List<VentaResponseDTO>> listarBorradores() {
        List<VentaResponseDTO> ventas = ventaService.listarBorradores();
        return ResponseEntity.ok(ventas);
    }

    // ========== LISTAR COMPLETADAS ==========

    /**
     * Listar solo ventas COMPLETADAS
     * GET /ventas/completadas
     */
    @GetMapping("/completadas")
    public ResponseEntity<List<VentaResponseDTO>> listarCompletadas() {
        List<VentaResponseDTO> ventas = ventaService.listarCompletadas();
        return ResponseEntity.ok(ventas);
    }

    // ========== LISTAR POR RANGO DE FECHAS ==========

    /**
     * Listar ventas por rango de fechas
     * GET /ventas/fechas?inicio=2025-01-01T00:00:00&fin=2025-01-31T23:59:59
     */
    @GetMapping("/fechas")
    public ResponseEntity<List<VentaResponseDTO>> listarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        List<VentaResponseDTO> ventas = ventaService.listarPorFecha(inicio, fin);
        return ResponseEntity.ok(ventas);
    }

    // ========== BUSCAR POR CLIENTE ==========

    /**
     * Buscar ventas por nombre de cliente
     * GET /ventas/buscar?cliente=Juan
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<VentaResponseDTO>> buscarPorCliente(@RequestParam String cliente) {
        List<VentaResponseDTO> ventas = ventaService.buscarPorCliente(cliente);
        return ResponseEntity.ok(ventas);
    }

    // ========== ACTUALIZAR VENTA ==========

    /**
     * Actualizar una venta (solo si está en BORRADOR)
     * PUT /ventas/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> actualizarVenta(
            @PathVariable Integer id,
            @Valid @RequestBody VentaRequestDTO request) {
        try {
            VentaResponseDTO response = ventaService.actualizarVenta(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ========== ELIMINAR VENTA ==========

    /**
     * Eliminar una venta (solo si está en BORRADOR)
     * DELETE /ventas/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarVenta(@PathVariable Integer id) {
        try {
            ventaService.eliminarVenta(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ========== ESTADÍSTICAS ==========

    /**
     * Contar ventas por estado
     * GET /ventas/estadisticas/estado/{estado}
     */
    @GetMapping("/estadisticas/estado/{estado}")
    public ResponseEntity<Long> contarPorEstado(@PathVariable EstadoVenta estado) {
        Long count = ventaService.contarVentasPorEstado(estado);
        return ResponseEntity.ok(count);
    }

    // ========== ENDPOINT DE PRUEBA ==========

    /**
     * Verificar que el módulo funciona
     * GET /ventas/salud
     */
    @GetMapping("/salud")
    public ResponseEntity<String> salud() {
        return ResponseEntity.ok("✅ Módulo Ventas funcionando correctamente");
    }
}