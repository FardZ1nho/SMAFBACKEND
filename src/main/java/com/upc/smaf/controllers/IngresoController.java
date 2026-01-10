package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.IngresoRequestDTO;
import com.upc.smaf.dtos.response.IngresoResponseDTO;
import com.upc.smaf.serviceinterface.IngresoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingresos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Ajusta esto según tu configuración de seguridad
public class IngresoController {

    private final IngresoService ingresoService;

    /**
     * POST /api/ingresos
     * Registra un nuevo ingreso de mercadería
     */
    @PostMapping
    public ResponseEntity<IngresoResponseDTO> registrarIngreso(@RequestBody IngresoRequestDTO request) {
        IngresoResponseDTO response = ingresoService.registrarIngreso(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * GET /api/ingresos
     * Obtiene todo el historial de ingresos
     */
    @GetMapping
    public ResponseEntity<List<IngresoResponseDTO>> listarHistorial() {
        List<IngresoResponseDTO> lista = ingresoService.listarHistorialIngresos();
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }

    /**
     * GET /api/ingresos/producto/{id}
     * Opcional: Obtener ingresos de un producto específico
     */
    @GetMapping("/producto/{id}")
    public ResponseEntity<List<IngresoResponseDTO>> listarPorProducto(@PathVariable Integer id) {
        List<IngresoResponseDTO> lista = ingresoService.listarIngresosPorProducto(id);
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }
}