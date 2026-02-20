package com.upc.smaf.dtos.request;

import lombok.Data;

@Data
public class RecepcionRequestDTO {
    private Integer detalleId; // El ID del CompraDetalle
    private Integer cantidadRecibida;
    private Integer almacenId; // Para saber en qué almacén se guardó
}