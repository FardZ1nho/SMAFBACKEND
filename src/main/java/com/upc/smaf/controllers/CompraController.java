package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.CompraRequestDTO;
import com.upc.smaf.dtos.response.CompraResponseDTO;
import com.upc.smaf.serviceinterface.CompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/compras")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CompraController {

    private final CompraService compraService;

    /**
     * POST /compras
     * Registra una nueva compra con sus detalles y actualiza el stock.
     */
    @PostMapping
    public ResponseEntity<CompraResponseDTO> registrarCompra(@RequestBody CompraRequestDTO request) {
        try {
            CompraResponseDTO response = compraService.registrarCompra(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            // Devuelve un error 400 con el mensaje de la excepción (ej: "Producto no encontrado")
            return ResponseEntity.badRequest().header("Error-Message", e.getMessage()).build();
        }
    }

    /**
     * GET /compras/{id}
     * Obtiene los datos de una compra específica.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CompraResponseDTO> obtenerCompra(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(compraService.obtenerCompra(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /compras
     * Lista el historial de todas las compras.
     */
    @GetMapping
    public ResponseEntity<List<CompraResponseDTO>> listarTodas() {
        return ResponseEntity.ok(compraService.listarTodas());
    }

    /**
     * GET /compras/proveedor/{proveedorId}
     * Filtra compras por un proveedor específico.
     */
    @GetMapping("/proveedor/{proveedorId}")
    public ResponseEntity<List<CompraResponseDTO>> listarPorProveedor(@PathVariable Integer proveedorId) {
        return ResponseEntity.ok(compraService.listarPorProveedor(proveedorId));
    }

    /**
     * GET /compras/buscar?numero=...
     * Busca compras por coincidencia en el número de comprobante.
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<CompraResponseDTO>> buscarPorNumero(@RequestParam String numero) {
        return ResponseEntity.ok(compraService.buscarPorNumero(numero));
    }
}