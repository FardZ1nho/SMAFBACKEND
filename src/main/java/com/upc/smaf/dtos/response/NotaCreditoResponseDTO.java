package com.upc.smaf.dtos.response;

import com.upc.smaf.entities.MotivoNota;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class NotaCreditoResponseDTO {
    private Integer id;
    private String codigoCompleto; // Serie + Numero (NC01-00023)
    private String codigoVentaAfectada;
    private MotivoNota motivo;
    private BigDecimal montoTotal;
    private String moneda;
    private LocalDateTime fechaEmision;
    private String observaciones;
}