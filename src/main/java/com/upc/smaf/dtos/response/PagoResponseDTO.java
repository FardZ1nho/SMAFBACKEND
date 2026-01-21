package com.upc.smaf.dtos.response;

import com.upc.smaf.entities.MetodoPago;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PagoResponseDTO {
    private Integer id;
    private BigDecimal monto;
    private String moneda;
    private MetodoPago metodoPago;
    private LocalDateTime fechaPago;
    private String referencia;
    private String nombreCuentaDestino;
}