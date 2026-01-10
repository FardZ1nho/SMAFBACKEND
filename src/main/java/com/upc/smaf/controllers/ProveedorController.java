package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.ProveedorRequestDTO;
import com.upc.smaf.dtos.response.ProveedorResponseDTO;
import com.upc.smaf.serviceinterface.ProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/proveedores")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ProveedorController {

    private final ProveedorService proveedorService;

    /**
     * Crear un nuevo proveedor
     * POST /proveedores
     */
    @PostMapping
    public ResponseEntity<ProveedorResponseDTO> crearProveedor(
            @Valid @RequestBody ProveedorRequestDTO request) {
        try {
            ProveedorResponseDTO response = proveedorService.crearProveedor(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener proveedor por ID
     * GET /proveedores/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProveedorResponseDTO> obtenerProveedor(@PathVariable Integer id) {
        try {
            ProveedorResponseDTO response = proveedorService.obtenerProveedor(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Listar todos los proveedores
     * GET /proveedores
     */
    @GetMapping
    public ResponseEntity<List<ProveedorResponseDTO>> listarProveedores() {
        List<ProveedorResponseDTO> proveedores = proveedorService.listarProveedores();
        return ResponseEntity.ok(proveedores);
    }

    /**
     * Listar solo proveedores activos
     * GET /proveedores/activos
     */
    @GetMapping("/activos")
    public ResponseEntity<List<ProveedorResponseDTO>> listarProveedoresActivos() {
        List<ProveedorResponseDTO> proveedores = proveedorService.listarProveedoresActivos();
        return ResponseEntity.ok(proveedores);
    }

    /**
     * Actualizar proveedor
     * PUT /proveedores/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProveedorResponseDTO> actualizarProveedor(
            @PathVariable Integer id,
            @Valid @RequestBody ProveedorRequestDTO request) {
        try {
            ProveedorResponseDTO response = proveedorService.actualizarProveedor(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Desactivar proveedor (eliminación lógica)
     * DELETE /proveedores/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarProveedor(@PathVariable Integer id) {
        try {
            proveedorService.desactivarProveedor(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Buscar proveedores por nombre
     * GET /proveedores/buscar?nombre=ejemplo
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ProveedorResponseDTO>> buscarPorNombre(
            @RequestParam String nombre) {
        List<ProveedorResponseDTO> proveedores = proveedorService.buscarPorNombre(nombre);
        return ResponseEntity.ok(proveedores);
    }

    /**
     * Obtener proveedor por RUC
     * GET /proveedores/ruc/{ruc}
     */
    @GetMapping("/ruc/{ruc}")
    public ResponseEntity<ProveedorResponseDTO> obtenerPorRuc(@PathVariable String ruc) {
        try {
            ProveedorResponseDTO response = proveedorService.obtenerPorRuc(ruc);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}