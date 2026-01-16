package com.upc.smaf.controllers;

import com.upc.smaf.entities.CuentaBancaria;
import com.upc.smaf.serviceinterface.CuentaBancariaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cuentas-bancarias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Para evitar bloqueos de CORS
public class CuentaBancariaController {

    private final CuentaBancariaService cuentaService;

    @GetMapping
    public ResponseEntity<List<CuentaBancaria>> listarTodas() {
        return ResponseEntity.ok(cuentaService.listarTodas());
    }

    @GetMapping("/activas")
    public ResponseEntity<List<CuentaBancaria>> listarActivas() {
        return ResponseEntity.ok(cuentaService.listarActivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(cuentaService.obtenerPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<CuentaBancaria> crear(@RequestBody CuentaBancaria cuenta) {
        return ResponseEntity.ok(cuentaService.guardar(cuenta));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody CuentaBancaria cuenta) {
        try {
            return ResponseEntity.ok(cuentaService.actualizar(id, cuenta));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            cuentaService.eliminar(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // Si falla por FK (ya tiene pagos), sugerimos desactivar
            return ResponseEntity.badRequest().body(Map.of("message", "No se puede eliminar porque tiene pagos asociados. Intente desactivarla."));
        }
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivar(@PathVariable Integer id) {
        try {
            cuentaService.desactivar(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}