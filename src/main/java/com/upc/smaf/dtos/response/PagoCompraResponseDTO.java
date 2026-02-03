package com.upc.smaf.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PagoCompraResponseDTO {
    // CAMBIO: Long -> Integer
    private Integer id;

    private BigDecimal monto;
    private String moneda;
    private String metodoPago;
    private LocalDateTime fechaPago;
    private String referencia;
    private String nombreCuentaOrigen;
}