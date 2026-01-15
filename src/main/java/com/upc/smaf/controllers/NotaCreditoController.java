package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.NotaCreditoRequestDTO;
import com.upc.smaf.dtos.response.NotaCreditoResponseDTO;
import com.upc.smaf.serviceinterface.NotaCreditoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/notas-credito")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Ajusta según tu seguridad
public class NotaCreditoController {

    private final NotaCreditoService notaCreditoService;

    // Crear nueva Nota de Crédito
    @PostMapping
    public ResponseEntity<NotaCreditoResponseDTO> emitirNota(@Valid @RequestBody NotaCreditoRequestDTO request) {
        try {
            return new ResponseEntity<>(notaCreditoService.emitirNotaCredito(request), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null); // O manejar error personalizado
        }
    }

    // Listar todas (Para la pestaña separada que te recomendé)
    @GetMapping
    public ResponseEntity<List<NotaCreditoResponseDTO>> listarTodas() {
        return ResponseEntity.ok(notaCreditoService.listarTodas());
    }

    // Ver notas de una venta específica
    @GetMapping("/venta/{ventaId}")
    public ResponseEntity<List<NotaCreditoResponseDTO>> verPorVenta(@PathVariable Integer ventaId) {
        return ResponseEntity.ok(notaCreditoService.listarPorVenta(ventaId));
    }

    // ⭐ Endpoint vital para tu Dashboard (Ingreso Neto)
    @GetMapping("/total-global")
    public ResponseEntity<BigDecimal> obtenerTotalGlobalDevoluciones() {
        return ResponseEntity.ok(notaCreditoService.obtenerTotalDevoluciones());
    }
}