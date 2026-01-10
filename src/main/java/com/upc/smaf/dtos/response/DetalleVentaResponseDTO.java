package com.upc.smaf.dtos.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetalleVentaResponseDTO {

    private Integer id;
    private Integer productoId;
    private String productoNombre;
    private String productoCodigo;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal descuento;
    private BigDecimal subtotal;
}