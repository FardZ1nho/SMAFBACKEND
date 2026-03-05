package com.upc.smaf.dtos.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MovimientoCajaRequestDTO {
    private String tipo; // "INGRESO" o "EGRESO"
    private BigDecimal monto;
    private String motivo;
    private String responsable;
    private LocalDateTime fechaHora; // Opcional, por si quieren registrar un movimiento de ayer

    // En tu ResponseDTO y RequestDTO, simplemente añade:
    private String categoria;
    private Integer turnoCajaId; // Para saber en qué turno se hizo
}