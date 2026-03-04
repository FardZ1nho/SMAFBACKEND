package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.MovimientoCajaRequestDTO;
import com.upc.smaf.dtos.response.MovimientoCajaResponseDTO;
import com.upc.smaf.serviceinterface.MovimientoCajaService;
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
    private final MovimientoCajaService movimientoCajaService;

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
        MovimientoCajaResponseDTO actualizado = movimientoCajaService.actualizarMovimiento(id, request);
        return ResponseEntity.ok(actualizado);
    }
}