package com.upc.smaf.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@RestController
@RequestMapping("/api/consultas")
@CrossOrigin(origins = "*")
public class ConsultaController {

    // Jalamos el token desde el application.properties
    @Value("${api.peru.token}")
    private String apiToken;

    @GetMapping("/documento/{numero}")
    public ResponseEntity<?> consultarDocumento(@PathVariable String numero) {

        // 1. Validamos la longitud (8 para DNI, 11 para RUC)
        if (numero == null || (numero.length() != 8 && numero.length() != 11)) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El documento debe tener 8 o 11 dígitos"));
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = "";

        // 2. Elegimos la ruta de la API externa (Cambiado a DECOLECTA)
        if (numero.length() == 8) {
            // OJO: Asumo esta ruta para DNI, pero deberías revisar tu manual de Decolecta
            // para confirmar si la ruta exacta del DNI es /v1/reniec/dni
            url = "https://api.decolecta.com/v1/reniec/dni?numero=" + numero;
        } else {
            // Ruta oficial de RUC según tu manual
            url = "https://api.decolecta.com/v1/sunat/ruc?numero=" + numero;
        }

        // 3. Preparamos las cabeceras requeridas por Decolecta
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiToken);
        headers.set("Content-Type", "application/json"); // Faltaba esta línea

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Hacemos la consulta a DECOLECTA
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            System.out.println("❌ ERROR API DECOLECTA: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Documento no encontrado o error de token"));
        }
    }
}