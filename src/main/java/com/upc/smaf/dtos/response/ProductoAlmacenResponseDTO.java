package com.upc.smaf.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoAlmacenResponseDTO {

    private Long id;

    // Información del producto
    private Integer productoId;
    private String productoNombre;
    private String productoCodigo;

    // Información del almacén
    private Long almacenId;
    private String almacenCodigo;
    private String almacenNombre;

    // Stock específico en este almacén
    private Integer stock;
    private String ubicacionFisica;
    private Integer stockMinimo;

    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}