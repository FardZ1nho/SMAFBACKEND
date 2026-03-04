package com.upc.smaf.controllers;

import com.upc.smaf.dtos.response.FinanzasDashboardResponseDTO;
import com.upc.smaf.serviceinterface.FinanzasService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@CrossOrigin(origins = "*") // ✅ Permite peticiones desde cualquier Frontend (Angular, React, etc.)
@RestController
@RequestMapping("/finanzas") // ✅ Se quitó el "/api"
@RequiredArgsConstructor
public class FinanzasController {

    private final FinanzasService finanzasService;

    // Llamada: GET http://localhost:8080/finanzas/dashboard?inicio=2026-03-01&fin=2026-03-31
    @GetMapping("/dashboard")
    public ResponseEntity<FinanzasDashboardResponseDTO> obtenerDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin
    ) {
        FinanzasDashboardResponseDTO dashboard = finanzasService.obtenerDashboardFinanciero(inicio, fin);
        return ResponseEntity.ok(dashboard);
    }
}