package com.upc.smaf.dtos.request;

import com.upc.smaf.entities.MotivoNota;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class NotaCreditoRequestDTO {

    @NotNull(message = "Debe indicar la venta a afectar")
    private Integer ventaId;

    @NotNull(message = "El motivo es obligatorio")
    private MotivoNota motivo;

    @NotNull
    @Positive(message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    private String observaciones;
}