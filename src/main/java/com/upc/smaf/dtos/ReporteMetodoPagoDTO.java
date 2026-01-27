package com.upc.smaf.dtos;

import com.upc.smaf.entities.MetodoPago;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor // ✅ Genera el constructor que usaremos en la Query
@NoArgsConstructor
public class ReporteMetodoPagoDTO {
    private MetodoPago metodo;
    private BigDecimal total;
    private Long cantidad;
}