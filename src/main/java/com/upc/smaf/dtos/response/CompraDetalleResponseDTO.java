package com.upc.smaf.dtos.response;

import lombok.Data;

@Data
public class CompraDetalleResponseDTO {
    // Estos son los campos que faltaban y causaban el error rojo
    private Integer productoId;
    private String nombreProducto;
    private String codigoProducto;

    // Estos probablemente ya los tenías
    private Integer cantidad;
    private Double precioUnitario;
}