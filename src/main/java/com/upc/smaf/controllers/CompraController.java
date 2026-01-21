package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.CompraRequestDTO;
import com.upc.smaf.dtos.response.CompraResponseDTO;
import com.upc.smaf.serviceinterface.CompraService;
import jakarta.validation.Valid; // ✅ Importante para validar el DTO
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/compras")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CompraController {

    private final CompraService compraService;

    // POST: Registra una compra (Bien o Servicio)
    @PostMapping
    public ResponseEntity<?> registrarCompra(@Valid @RequestBody CompraRequestDTO request) {
        try {
            return new ResponseEntity<>(compraService.registrarCompra(request), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Retorna el mensaje de error directo (ej. "Proveedor no encontrado")
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET: Obtiene una compra por ID
    @GetMapping("/{id}")
    public ResponseEntity<CompraResponseDTO> obtenerCompra(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(compraService.obtenerCompra(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET: Lista todo el historial
    @GetMapping
    public ResponseEntity<List<CompraResponseDTO>> listarTodas() {
        return ResponseEntity.ok(compraService.listarTodas());
    }

    // GET: Filtra por proveedor
    @GetMapping("/proveedor/{proveedorId}")
    public ResponseEntity<List<CompraResponseDTO>> listarPorProveedor(@PathVariable Integer proveedorId) {
        return ResponseEntity.ok(compraService.listarPorProveedor(proveedorId));
    }

    // GET: Busca por número de documento (ej. /compras/buscar?numero=F001)
    @GetMapping("/buscar")
    public ResponseEntity<List<CompraResponseDTO>> buscarPorNumero(@RequestParam String numero) {
        return ResponseEntity.ok(compraService.buscarPorNumero(numero));
    }
}