package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.TareaCrmRequestDTO;
import com.upc.smaf.dtos.response.TareaCrmResponseDTO;
import com.upc.smaf.serviceinterface.TareaCrmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tareascrm") // Quitamos el /api
@CrossOrigin(origins = "*") // Abierto para cualquier origen
public class TareaCrmController {

    @Autowired
    private TareaCrmService tareaCrmService;

    // 1. Crear una nueva tarea para una cotización
    @PostMapping
    public ResponseEntity<TareaCrmResponseDTO> crearTarea(@RequestBody TareaCrmRequestDTO requestDTO) {
        return new ResponseEntity<>(tareaCrmService.crearTarea(requestDTO), HttpStatus.CREATED);
    }

    // 2. Obtener todas las tareas de una cotización específica
    @GetMapping("/cotizacion/{cotizacionId}")
    public ResponseEntity<List<TareaCrmResponseDTO>> obtenerPorCotizacion(@PathVariable Integer cotizacionId) {
        return ResponseEntity.ok(tareaCrmService.obtenerTareasPorCotizacion(cotizacionId));
    }

    // 3. Obtener todas las tareas pendientes
    @GetMapping("/pendientes")
    public ResponseEntity<List<TareaCrmResponseDTO>> obtenerPendientes() {
        return ResponseEntity.ok(tareaCrmService.obtenerTareasPendientes());
    }

    // 4. Marcar una tarea como completada
    @PutMapping("/{id}/completar")
    public ResponseEntity<TareaCrmResponseDTO> completarTarea(@PathVariable Long id) {
        return ResponseEntity.ok(tareaCrmService.marcarComoCompletada(id));
    }

    // 5. Eliminar una tarea
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTarea(@PathVariable Long id) {
        tareaCrmService.eliminarTarea(id);
        return ResponseEntity.noContent().build();
    }
}