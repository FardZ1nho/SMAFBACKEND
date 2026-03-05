package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.MovimientoCajaRequestDTO;
import com.upc.smaf.dtos.response.MovimientoCajaResponseDTO;
import com.upc.smaf.repositories.TurnoCajaRepository;
import com.upc.smaf.serviceinterface.MovimientoCajaService;
import com.upc.smaf.servicesimplements.TurnoCajaServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cajachica")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MovimientoCajaController {

    private final MovimientoCajaService service;

    // ✅ CORRECCIÓN 1: Usamos 'private final' para que Lombok los inyecte automáticamente
    // sin necesidad de usar @Autowired, manteniendo tu código limpio.
    private final TurnoCajaServiceImpl turnoCajaService;
    private final TurnoCajaRepository turnoCajaRepository;

    @PostMapping
    public ResponseEntity<MovimientoCajaResponseDTO> registrar(@RequestBody MovimientoCajaRequestDTO request) {
        return ResponseEntity.ok(service.registrarMovimiento(request));
    }

    @GetMapping
    public ResponseEntity<List<MovimientoCajaResponseDTO>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovimientoCajaResponseDTO> actualizarMovimiento(
            @PathVariable Integer id,
            @Valid @RequestBody MovimientoCajaRequestDTO request) {
        MovimientoCajaResponseDTO actualizado = service.actualizarMovimiento(id, request);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMovimiento(@PathVariable Integer id) {
        service.eliminarMovimiento(id);
        return ResponseEntity.noContent().build();
    }

    // =================================================================
    // NUEVOS ENDPOINTS PARA TURNOS DE CAJA Y BANCOS
    // =================================================================

    @GetMapping("/turnos/activo")
    public ResponseEntity<?> obtenerTurnoActivo() {
        return turnoCajaRepository.findByEstado("ABIERTO")
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/turnos/abrir")
    public ResponseEntity<?> abrirCaja(@RequestBody java.util.Map<String, Object> payload) {
        java.math.BigDecimal saldoInicial = new java.math.BigDecimal(payload.get("saldoInicial").toString());
        String responsable = payload.get("responsable").toString();
        return ResponseEntity.ok(turnoCajaService.abrirCaja(saldoInicial, responsable));
    }

    @PostMapping("/turnos/cerrar")
    public ResponseEntity<?> cerrarCaja(@RequestBody java.util.Map<String, Object> payload) {
        java.math.BigDecimal saldoFisico = new java.math.BigDecimal(payload.get("saldoFisico").toString());
        return ResponseEntity.ok(turnoCajaService.cerrarCaja(saldoFisico));
    }

    @PostMapping("/depositar")
    public ResponseEntity<?> depositarABanco(@RequestBody java.util.Map<String, Object> payload) {
        java.math.BigDecimal monto = new java.math.BigDecimal(payload.get("monto").toString());
        Integer cuentaId = Integer.parseInt(payload.get("cuentaId").toString());
        String responsable = payload.get("responsable").toString();

        // ✅ CORRECCIÓN 2: Se usa "service." en lugar de "movimientoCajaService."
        return ResponseEntity.ok(service.depositarABanco(monto, cuentaId, responsable));
    }
}