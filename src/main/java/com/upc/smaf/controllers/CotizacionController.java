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

    // Crear Cotización
    @PostMapping
    public ResponseEntity<Cotizacion> crear(@RequestBody CotizacionRequestDTO dto) {
        return ResponseEntity.ok(cotizacionService.registrar(dto));
    }

    // Listar Todas
    @GetMapping
    public ResponseEntity<List<Cotizacion>> listar() {
        return ResponseEntity.ok(cotizacionService.listarTodas());
    }

    // ✅ DESCARGAR PDF
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Integer id) {
        byte[] pdfBytes = cotizacionService.obtenerPdf(id);

        // Cabeceras HTTP para decirle al navegador que esto es un PDF
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        // "inline" = abre en pestaña nueva. "attachment" = fuerza descarga.
        headers.setContentDispositionFormData("inline", "cotizacion-" + id + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    // Aprobar
    @PostMapping("/{id}/aprobar")
    public ResponseEntity<Void> aprobar(@PathVariable Integer id) {
        cotizacionService.aprobarCotizacion(id);
        return ResponseEntity.ok().build();
    }
}