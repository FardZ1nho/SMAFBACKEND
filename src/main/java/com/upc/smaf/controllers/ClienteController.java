package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.ClienteRequestDTO;
import com.upc.smaf.dtos.response.ClienteResponseDTO;
import com.upc.smaf.serviceinterface.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    /**
     * Crear un nuevo cliente
     * POST /clientes
     */
    @PostMapping
    public ResponseEntity<?> crearCliente( // 👈 Cambiado a <?> para poder devolver errores
                                           @Valid @RequestBody ClienteRequestDTO request) {
        try {
            ClienteResponseDTO response = clienteService.crearCliente(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            // ✅ ESTO ES LO QUE ARREGLA TU PROBLEMA:
            // Ahora devolvemos el mensaje del error para que Angular lo pueda mostrar
            return ResponseEntity.badRequest().body(Collections.singletonMap("mensaje", e.getMessage()));
        }
    }

    /**
     * Obtener cliente por ID
     * GET /clientes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> obtenerCliente(@PathVariable Integer id) {
        try {
            ClienteResponseDTO response = clienteService.obtenerCliente(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Listar todos los clientes
     * GET /clientes
     */
    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarClientes() {
        List<ClienteResponseDTO> clientes = clienteService.listarClientes();
        return ResponseEntity.ok(clientes);
    }

    /**
     * Listar solo clientes activos
     * GET /clientes/activos
     */
    @GetMapping("/activos")
    public ResponseEntity<List<ClienteResponseDTO>> listarClientesActivos() {
        List<ClienteResponseDTO> clientes = clienteService.listarClientesActivos();
        return ResponseEntity.ok(clientes);
    }

    /**
     * Actualizar cliente
     * PUT /clientes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCliente( // 👈 Cambiado a <?> también aquí
                                                @PathVariable Integer id,
                                                @Valid @RequestBody ClienteRequestDTO request) {
        try {
            ClienteResponseDTO response = clienteService.actualizarCliente(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // ✅ También mostramos el error al editar
            return ResponseEntity.badRequest().body(Collections.singletonMap("mensaje", e.getMessage()));
        }
    }

    /**
     * Desactivar cliente (eliminación lógica)
     * DELETE /clientes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarCliente(@PathVariable Integer id) {
        try {
            clienteService.desactivarCliente(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Buscar cliente por documento
     * GET /clientes/documento/{numeroDocumento}
     */
    @GetMapping("/documento/{numeroDocumento}")
    public ResponseEntity<ClienteResponseDTO> obtenerClientePorDocumento(
            @PathVariable String numeroDocumento) {
        try {
            ClienteResponseDTO response = clienteService.obtenerClientePorDocumento(numeroDocumento);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Buscar clientes por nombre
     * GET /clientes/buscar-nombre?nombre=juan
     */
    @GetMapping("/buscar-nombre")
    public ResponseEntity<List<ClienteResponseDTO>> buscarClientesPorNombre(
            @RequestParam String nombre) {
        List<ClienteResponseDTO> clientes = clienteService.buscarClientesPorNombre(nombre);
        return ResponseEntity.ok(clientes);
    }

    /**
     * Buscar clientes (nombre, documento o email)
     * GET /clientes/buscar?termino=juan
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteResponseDTO>> buscarClientes(
            @RequestParam String termino) {
        List<ClienteResponseDTO> clientes = clienteService.buscarClientes(termino);
        return ResponseEntity.ok(clientes);
    }

    /**
     * Listar clientes por tipo
     * GET /clientes/tipo/{tipoCliente}
     */
    @GetMapping("/tipo/{tipoCliente}")
    public ResponseEntity<List<ClienteResponseDTO>> listarClientesPorTipo(
            @PathVariable String tipoCliente) {
        List<ClienteResponseDTO> clientes = clienteService.listarClientesPorTipo(tipoCliente);
        return ResponseEntity.ok(clientes);
    }
}