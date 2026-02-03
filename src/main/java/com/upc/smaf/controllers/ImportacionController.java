package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.ImportacionRequestDTO;
import com.upc.smaf.dtos.response.ImportacionResponseDTO;
import com.upc.smaf.entities.EstadoImportacion;
import com.upc.smaf.serviceinterface.ImportacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/importaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ImportacionController {

    private final ImportacionService importacionService;

    // Listar todas
    @GetMapping
    public ResponseEntity<List<ImportacionResponseDTO>> listarTodas() {
        return ResponseEntity.ok(importacionService.listarTodas());
    }

    // Filtrar por estado
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ImportacionResponseDTO>> listarPorEstado(@PathVariable EstadoImportacion estado) {
        return ResponseEntity.ok(importacionService.listarPorEstado(estado));
    }

    // Obtener por ID numérico
    @GetMapping("/{id}")
    public ResponseEntity<ImportacionResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(importacionService.obtenerPorId(id));
    }

    // Buscar por Código de Texto
    @GetMapping("/buscar/{codigo}")
    public ResponseEntity<ImportacionResponseDTO> obtenerPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(importacionService.obtenerPorCodigo(codigo));
    }

    // ✅ CORREGIDO: CREAR (POST) -> Llama a guardar()
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody ImportacionRequestDTO request) {
        try {
            return ResponseEntity.ok(importacionService.guardar(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ✅ CORREGIDO: ACTUALIZAR (PUT) -> Llama a actualizar()
    // Esto es lo que usa tu Modal de Edición cuando le das "Guardar"
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody ImportacionRequestDTO request) {
        try {
            return ResponseEntity.ok(importacionService.actualizar(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Recálculo manual
    @PostMapping("/{id}/recalcular")
    public ResponseEntity<Void> recalcularCostos(@PathVariable Integer id) {
        importacionService.recalcularCostos(id);
        return ResponseEntity.ok().build();
    }
}