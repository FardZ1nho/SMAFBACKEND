package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.VentaRequestDTO;
import com.upc.smaf.dtos.response.VentaResponseDTO;
import com.upc.smaf.entities.EstadoVenta;
import com.upc.smaf.entities.MetodoPago;
import com.upc.smaf.serviceinterface.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ventas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VentaController {

    private final VentaService ventaService;

    // ========== CREAR VENTA COMPLETA ==========
    @PostMapping
    public ResponseEntity<?> crearVenta(@Valid @RequestBody VentaRequestDTO request) {
        try {
            VentaResponseDTO response = ventaService.crearVenta(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ========== GUARDAR BORRADOR ==========
    @PostMapping("/borrador")
    public ResponseEntity<?> guardarBorrador(@Valid @RequestBody VentaRequestDTO request) {
        try {
            VentaResponseDTO response = ventaService.guardarBorrador(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ========== ✅ ACTUALIZADO: REGISTRAR PAGO (AMORTIZACIÓN) ==========
    /**
     * Ahora acepta cuentaId opcionalmente.
     * Ejemplo: POST /ventas/5/pagos?monto=100.00&metodo=YAPE&cuentaId=2
     */
    @PostMapping("/{id}/pagos")
    public ResponseEntity<?> registrarPago(
            @PathVariable Integer id,
            @RequestParam BigDecimal monto,
            @RequestParam MetodoPago metodo,
            // ✅ AGREGADO: Parámetro opcional para la cuenta destino
            @RequestParam(required = false) Integer cuentaId) {
        try {
            VentaResponseDTO response = ventaService.registrarAmortizacion(id, monto, metodo, cuentaId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ========== COMPLETAR VENTA (DESDE BORRADOR) ==========
    @PostMapping("/{id}/completar")
    public ResponseEntity<?> completarVenta(@PathVariable Integer id) {
        try {
            VentaResponseDTO response = ventaService.completarVenta(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ========== CANCELAR VENTA ==========
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarVenta(@PathVariable Integer id) {
        try {
            ventaService.cancelarVenta(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ========== OBTENER VENTA ==========
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerVenta(@PathVariable Integer id) {
        try {
            VentaResponseDTO response = ventaService.obtenerVenta(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Venta no encontrada"));
        }
    }

    // ========== BUSCAR POR CÓDIGO ==========
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<?> buscarPorCodigo(@PathVariable String codigo) {
        try {
            VentaResponseDTO response = ventaService.buscarPorCodigo(codigo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Venta no encontrada"));
        }
    }

    // ========== LISTAR TODAS ==========
    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(ventaService.listarTodas());
    }

    // ========== LISTAR POR ESTADO ==========
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<VentaResponseDTO>> listarPorEstado(@PathVariable EstadoVenta estado) {
        return ResponseEntity.ok(ventaService.listarPorEstado(estado));
    }

    // ========== LISTAR BORRADORES ==========
    @GetMapping("/borradores")
    public ResponseEntity<List<VentaResponseDTO>> listarBorradores() {
        return ResponseEntity.ok(ventaService.listarBorradores());
    }

    // ========== LISTAR COMPLETADAS ==========
    @GetMapping("/completadas")
    public ResponseEntity<List<VentaResponseDTO>> listarCompletadas() {
        return ResponseEntity.ok(ventaService.listarCompletadas());
    }

    // ========== LISTAR POR FECHAS ==========
    @GetMapping("/fechas")
    public ResponseEntity<List<VentaResponseDTO>> listarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(ventaService.listarPorFecha(inicio, fin));
    }

    // ========== BUSCAR POR CLIENTE ==========
    @GetMapping("/buscar")
    public ResponseEntity<List<VentaResponseDTO>> buscarPorCliente(@RequestParam String cliente) {
        return ResponseEntity.ok(ventaService.buscarPorCliente(cliente));
    }

    // ========== ACTUALIZAR VENTA ==========
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarVenta(
            @PathVariable Integer id,
            @Valid @RequestBody VentaRequestDTO request) {
        try {
            VentaResponseDTO response = ventaService.actualizarVenta(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ========== ELIMINAR VENTA ==========
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarVenta(@PathVariable Integer id) {
        try {
            ventaService.eliminarVenta(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ========== ESTADÍSTICAS ==========
    @GetMapping("/estadisticas/estado/{estado}")
    public ResponseEntity<Long> contarPorEstado(@PathVariable EstadoVenta estado) {
        return ResponseEntity.ok(ventaService.contarVentasPorEstado(estado));
    }

    // ========== SALUD DEL SISTEMA ==========
    @GetMapping("/salud")
    public ResponseEntity<String> salud() {
        return ResponseEntity.ok("✅ Módulo Ventas funcionando correctamente");
    }
}