package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.ProveedorRequestDTO;
import com.upc.smaf.dtos.response.ProveedorResponseDTO;
import com.upc.smaf.serviceinterface.ProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/proveedores")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProveedorController {

    private final ProveedorService proveedorService;

    /**
     * Crear un nuevo proveedor (Nacional o Internacional)
     */
    @PostMapping
    public ResponseEntity<?> crearProveedor(@Valid @RequestBody ProveedorRequestDTO request) {
        try {
            ProveedorResponseDTO response = proveedorService.crearProveedor(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            // Captura errores de validación (Ej: RUC no tiene 11 dígitos o USCC no tiene 18)
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (DataIntegrityViolationException e) {
            String mensajeTecnico = e.getMostSpecificCause().getMessage().toLowerCase();
            String mensajeUsuario = "Error en la base de datos.";

            if (mensajeTecnico.contains("ruc")) {
                mensajeUsuario = "La identificación (RUC/USCC) ya existe en el sistema.";
            } else if (mensajeTecnico.contains("nombre")) {
                mensajeUsuario = "Ya existe un proveedor con esa Razón Social.";
            }

            return ResponseEntity.badRequest().body(Map.of("message", mensajeUsuario));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error interno: " + e.getMessage()));
        }
    }

    /**
     * Actualizar proveedor existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProveedor(
            @PathVariable Integer id,
            @Valid @RequestBody ProveedorRequestDTO request) {
        try {
            ProveedorResponseDTO response = proveedorService.actualizarProveedor(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Captura errores de validación de formato por país
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "La identificación ya pertenece a otro proveedor."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Listar todos los proveedores
     */
    @GetMapping
    public ResponseEntity<List<ProveedorResponseDTO>> listarProveedores() {
        return ResponseEntity.ok(proveedorService.listarProveedores());
    }

    /**
     * Listar solo proveedores activos
     */
    @GetMapping("/activos")
    public ResponseEntity<List<ProveedorResponseDTO>> listarProveedoresActivos() {
        return ResponseEntity.ok(proveedorService.listarProveedoresActivos());
    }

    /**
     * Obtener proveedor por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerProveedor(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(proveedorService.obtenerProveedor(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Desactivar proveedor (eliminación lógica)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivarProveedor(@PathVariable Integer id) {
        try {
            proveedorService.desactivarProveedor(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Buscar proveedores por nombre
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ProveedorResponseDTO>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(proveedorService.buscarPorNombre(nombre));
    }

    /**
     * Obtener proveedor por RUC o Identificación Internacional
     */
    @GetMapping("/ruc/{ruc}")
    public ResponseEntity<?> obtenerPorRuc(@PathVariable String ruc) {
        try {
            return ResponseEntity.ok(proveedorService.obtenerPorRuc(ruc));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
}