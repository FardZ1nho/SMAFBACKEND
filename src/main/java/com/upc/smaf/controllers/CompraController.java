package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.CompraRequestDTO;
import com.upc.smaf.dtos.response.CompraResponseDTO;
import com.upc.smaf.entities.MetodoPago;
import com.upc.smaf.serviceinterface.CompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/compras")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CompraController {

    private final CompraService compraService;

    @PostMapping
    public ResponseEntity<?> registrarCompra(@Valid @RequestBody CompraRequestDTO request) {
        try {
            return new ResponseEntity<>(compraService.registrarCompra(request), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ✅ ENDPOINT PARA PAGAR DEUDA (AMORTIZAR)
    // Ejemplo: POST /compras/10/pagos?monto=500&metodo=TRANSFERENCIA&cuentaId=1
    @PostMapping("/{id}/pagos")
    public ResponseEntity<?> registrarPago(
            @PathVariable Integer id,
            @RequestParam BigDecimal monto,
            @RequestParam MetodoPago metodo,
            @RequestParam(required = false) Integer cuentaId,
            @RequestParam(required = false) String referencia) {
        try {
            return ResponseEntity.ok(compraService.registrarAmortizacion(id, monto, metodo, cuentaId, referencia));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerCompra(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(compraService.obtenerCompra(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<CompraResponseDTO>> listarTodas() {
        return ResponseEntity.ok(compraService.listarTodas());
    }

    @GetMapping("/proveedor/{proveedorId}")
    public ResponseEntity<List<CompraResponseDTO>> listarPorProveedor(@PathVariable Integer proveedorId) {
        return ResponseEntity.ok(compraService.listarPorProveedor(proveedorId));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<CompraResponseDTO>> buscarPorNumero(@RequestParam String numero) {
        return ResponseEntity.ok(compraService.buscarPorNumero(numero));
    }
}