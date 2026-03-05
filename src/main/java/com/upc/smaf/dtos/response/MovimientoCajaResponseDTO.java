package com.upc.smaf.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MovimientoCajaResponseDTO {
    private Integer id;
    private String tipo;
    private BigDecimal monto;
    private String motivo;
    private String responsable;
    private LocalDateTime fechaHora;

    // En tu ResponseDTO y RequestDTO, simplemente añade:
    private String categoria;
    private Integer turnoCajaId; // Para saber en qué turno se hizo
}