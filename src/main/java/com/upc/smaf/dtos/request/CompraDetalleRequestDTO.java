package com.upc.smaf.dtos.request;

import lombok.Data;

@Data
public class CompraDetalleRequestDTO {
    private Integer productoId;
    private Integer almacenId; // Donde ingresará físicamente la mercadería
    private Integer cantidad;
    private Double precioUnitario; // Costo de compra
}