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
    private BigDecimal precioUnitario; // FOB Unitario
    private BigDecimal importe;        // FOB Total

    private String nombreAlmacen;

    // ✅ NUEVOS CAMPOS DE RESPUESTA
    private BigDecimal costoUnitarioLanded; // Costo real unitario
    private BigDecimal costoTotalLanded;    // Costo real total
}