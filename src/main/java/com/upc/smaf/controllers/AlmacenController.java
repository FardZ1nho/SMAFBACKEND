package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.AlmacenRequestDTO;
import com.upc.smaf.dtos.response.AlmacenResponseDTO;
import com.upc.smaf.serviceinterface.AlmacenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/almacenes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AlmacenController {

    private final AlmacenService almacenService;

    /**
     * Crear un nuevo almacén
     * POST /almacenes
     */
    @PostMapping
    public ResponseEntity<AlmacenResponseDTO> crearAlmacen(@Valid @RequestBody AlmacenRequestDTO request) {
        AlmacenResponseDTO almacen = almacenService.crearAlmacen(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(almacen);
    }

    /**
     * Actualizar un almacén existente
     * PUT /almacenes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<AlmacenResponseDTO> actualizarAlmacen(
            @PathVariable Long id,
            @Valid @RequestBody AlmacenRequestDTO request) {
        AlmacenResponseDTO almacen = almacenService.actualizarAlmacen(id, request);
        return ResponseEntity.ok(almacen);
    }

    /**
     * Obtener un almacén por ID
     * GET /almacenes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AlmacenResponseDTO> obtenerAlmacenPorId(@PathVariable Long id) {
        AlmacenResponseDTO almacen = almacenService.obtenerAlmacenPorId(id);
        return ResponseEntity.ok(almacen);
    }

    /**
     * Obtener un almacén por código
     * GET /almacenes/codigo/{codigo}
     */
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<AlmacenResponseDTO> obtenerAlmacenPorCodigo(@PathVariable String codigo) {
        AlmacenResponseDTO almacen = almacenService.obtenerAlmacenPorCodigo(codigo);
        return ResponseEntity.ok(almacen);
    }

    /**
     * Listar todos los almacenes
     * GET /almacenes
     */
    @GetMapping
    public ResponseEntity<List<AlmacenResponseDTO>> listarTodosLosAlmacenes() {
        List<AlmacenResponseDTO> almacenes = almacenService.listarTodosLosAlmacenes();
        return ResponseEntity.ok(almacenes);
    }

    /**
     * Listar solo almacenes activos
     * GET /almacenes/activos
     */
    @GetMapping("/activos")
    public ResponseEntity<List<AlmacenResponseDTO>> listarAlmacenesActivos() {
        List<AlmacenResponseDTO> almacenes = almacenService.listarAlmacenesActivos();
        return ResponseEntity.ok(almacenes);
    }

    /**
     * Cambiar estado de un almacén (activar/desactivar)
     * PATCH /almacenes/{id}/estado?activo=true
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstadoAlmacen(
            @PathVariable Long id,
            @RequestParam Boolean activo) {
        almacenService.cambiarEstadoAlmacen(id, activo);
        return ResponseEntity.ok().build();
    }

    /**
     * Eliminar un almacén (borrado físico)
     * DELETE /almacenes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAlmacen(@PathVariable Long id) {
        almacenService.eliminarAlmacen(id);
        return ResponseEntity.noContent().build();
    }
}