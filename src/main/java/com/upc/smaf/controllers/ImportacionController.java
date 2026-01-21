package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.ImportacionRequestDTO;
import com.upc.smaf.dtos.response.ImportacionResponseDTO;
import com.upc.smaf.entities.EstadoImportacion;
import com.upc.smaf.serviceinterface.ImportacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/importaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Para evitar bloqueos del frontend
public class ImportacionController {

    private final ImportacionService importacionService;

    @GetMapping
    public ResponseEntity<List<ImportacionResponseDTO>> listarTodas() {
        return ResponseEntity.ok(importacionService.listarTodas());
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ImportacionResponseDTO>> listarPorEstado(@PathVariable EstadoImportacion estado) {
        return ResponseEntity.ok(importacionService.listarPorEstado(estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImportacionResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(importacionService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImportacionResponseDTO> actualizar(
            @PathVariable Integer id,
            @RequestBody ImportacionRequestDTO request) {
        return ResponseEntity.ok(importacionService.actualizarImportacion(id, request));
    }
}