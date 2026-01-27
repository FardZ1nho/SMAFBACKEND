package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.TareaRequestDTO;
import com.upc.smaf.dtos.response.TareaResponseDTO;
import com.upc.smaf.entities.EstadoTarea;
import com.upc.smaf.serviceinterface.TareaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tareas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TareaController {

    private final TareaService tareaService;

    // ==========================================
    // 1. CREAR TAREA (Protegido: Solo ADMIN)
    // ==========================================
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')") // 🔒 SEGURIDAD REAL
    public ResponseEntity<?> crearTarea(
            @Valid @RequestBody TareaRequestDTO request,
            Authentication authentication) { // Spring inyecta al usuario logueado aquí
        try {
            // Pasamos el username del token al servicio
            String username = authentication.getName();
            TareaResponseDTO response = tareaService.crearTarea(request, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ==========================================
    // 2. LISTAR TAREAS (Inteligente)
    // ==========================================
    // No necesitamos parámetros de rol ni ID. El token lo dice todo.
    @GetMapping
    public ResponseEntity<List<TareaResponseDTO>> listarTareas(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(tareaService.listarTareas(username));
    }

    // ==========================================
    // 3. CAMBIAR ESTADO (Cualquier rol autenticado)
    // ==========================================
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Integer id,
            @RequestParam EstadoTarea estado,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            TareaResponseDTO response = tareaService.cambiarEstado(id, estado, username);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}