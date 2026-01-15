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

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<?> crearProveedor(@Valid @RequestBody ProveedorRequestDTO request) {
        try {
            ProveedorResponseDTO response = proveedorService.crearProveedor(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DataIntegrityViolationException e) {
            // Convertimos a minúsculas para facilitar la búsqueda
            String mensajeTecnico = e.getMostSpecificCause().getMessage().toLowerCase();
            System.out.println("⚠️ ERROR SQL DETECTADO: " + mensajeTecnico); // Míralo en consola

            String mensajeUsuario;

            if (mensajeTecnico.contains("ruc")) {
                mensajeUsuario = "El RUC ingresado ya existe en el sistema.";
            } else if (mensajeTecnico.contains("email") || mensajeTecnico.contains("correo")) {
                mensajeUsuario = "Ese correo electrónico ya está registrado.";
            } else if (mensajeTecnico.contains("telefono")) {
                mensajeUsuario = "Ese teléfono ya está registrado.";
            } else if (mensajeTecnico.contains("nombre") || mensajeTecnico.contains("razon")) {
                mensajeUsuario = "Ya existe un proveedor con esa Razón Social / Nombre.";
            } else {
                // AQUÍ EL CAMBIO CLAVE: Te mostrará el error real en la pantalla
                mensajeUsuario = "Error de base de datos desconocido: " + mensajeTecnico;
            }

            return ResponseEntity.badRequest().body(Map.of("message", mensajeUsuario));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", "Error interno: " + e.getMessage()));
        }
    }
    /**
     * Obtener proveedor por ID
     * GET /proveedores/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerProveedor(@PathVariable Integer id) {
        try {
            ProveedorResponseDTO response = proveedorService.obtenerProveedor(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Proveedor no encontrado"));
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
    public ResponseEntity<?> actualizarProveedor(
            @PathVariable Integer id,
            @Valid @RequestBody ProveedorRequestDTO request) {
        try {
            ProveedorResponseDTO response = proveedorService.actualizarProveedor(id, request);
            return ResponseEntity.ok(response);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "El RUC ingresado ya pertenece a otro proveedor."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "No se pudo actualizar: " + e.getMessage()));
        }
    }

    /**
     * Desactivar proveedor (eliminación lógica)
     * DELETE /proveedores/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivarProveedor(@PathVariable Integer id) {
        try {
            proveedorService.desactivarProveedor(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Proveedor no encontrado"));
        }
    }

    /**
     * Buscar proveedores por nombre
     * GET /proveedores/buscar?nombre=ejemplo
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ProveedorResponseDTO>> buscarPorNombre(@RequestParam String nombre) {
        List<ProveedorResponseDTO> proveedores = proveedorService.buscarPorNombre(nombre);
        return ResponseEntity.ok(proveedores);
    }

    /**
     * Obtener proveedor por RUC
     * GET /proveedores/ruc/{ruc}
     */
    @GetMapping("/ruc/{ruc}")
    public ResponseEntity<?> obtenerPorRuc(@PathVariable String ruc) {
        try {
            ProveedorResponseDTO response = proveedorService.obtenerPorRuc(ruc);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "No existe proveedor con ese RUC"));
        }
    }
}