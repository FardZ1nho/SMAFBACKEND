package com.upc.smaf.dtos.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CompraDetalleResponseDTO {
    private Integer id;
    private Integer productoId;

    private String nombreProducto;
    private String codigoProducto;

    private Integer cantidad;

    // ✅ NUEVO: La cantidad validada en almacén
    private Integer cantidadRecibida;

    private BigDecimal precioUnitario;
    private BigDecimal importe;

    private String nombreAlmacen;

    private BigDecimal costoUnitarioLanded;
    private BigDecimal costoTotalLanded;
}