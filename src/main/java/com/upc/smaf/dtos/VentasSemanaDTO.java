package com.upc.smaf.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentasSemanaDTO {
    private LocalDate fecha;
    private String diaSemana; // "Lunes", "Martes", etc.
    private BigDecimal totalVentas;
    private Long cantidadVentas;
}