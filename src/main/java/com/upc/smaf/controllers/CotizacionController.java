package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.CotizacionRequestDTO;
import com.upc.smaf.entities.Cotizacion;
import com.upc.smaf.servicesimplements.CotizacionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cotizaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Permite peticiones desde Angular
public class CotizacionController {

    private final CotizacionServiceImpl cotizacionService;

    // ========== CREAR COTIZACIÓN ==========
    @PostMapping
    public ResponseEntity<Cotizacion> crear(@RequestBody CotizacionRequestDTO dto) {
        return ResponseEntity.ok(cotizacionService.registrar(dto));
    }

    // ========== LISTAR TODAS ==========
    @GetMapping
    public ResponseEntity<List<Cotizacion>> listar() {
        return ResponseEntity.ok(cotizacionService.listarTodas());
    }

    // ========== DESCARGAR PDF ==========
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Integer id) {
        byte[] pdfBytes = cotizacionService.obtenerPdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "cotizacion-" + id + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    // ⭐⭐⭐ NUEVO: ACTUALIZAR ESTADO DEL PIPELINE (CRM) ⭐⭐⭐
    /**
     * Ejemplo de uso desde Angular:
     * PUT /cotizaciones/5/estado?estado=GANADA
     * PUT /cotizaciones/5/estado?estado=PERDIDA&motivoPerdida=Precio%20muy%20alto
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<Cotizacion> actualizarEstadoPipeline(
            @PathVariable Integer id,
            @RequestParam Cotizacion.EstadoCotizacion estado,
            @RequestParam(required = false) String motivoPerdida) {

        Cotizacion cotizacionActualizada = cotizacionService.actualizarEstadoPipeline(id, estado, motivoPerdida);
        return ResponseEntity.ok(cotizacionActualizada);
    }
}