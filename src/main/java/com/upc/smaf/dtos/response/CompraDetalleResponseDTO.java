package com.upc.smaf.dtos.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CompraDetalleResponseDTO {
    // CAMBIO: Long -> Integer
    private Integer id;
    private Integer productoId;

    private String nombreProducto;
    private String codigoProducto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal importe;
    private String nombreAlmacen;
}