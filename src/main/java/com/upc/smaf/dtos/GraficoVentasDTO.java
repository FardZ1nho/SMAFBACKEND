package com.upc.smaf.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraficoVentasDTO {
    private String label;      // Ej: "Lunes", "24/01", "Enero"
    private BigDecimal total;  // Total vendido
    private Long cantidad;     // Cantidad de transacciones
}