package com.upc.smaf.dtos.request;

import lombok.Data;

@Data
public class RecepcionItemDTO {
    private Integer detalleId;       // ID del CompraDetalle
    private Integer cantidadRecibida; // Lo que el almacenero digitó
}