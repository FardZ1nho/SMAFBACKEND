package com.upc.smaf.dtos.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CompraDetalleRequestDTO {
    private Integer productoId;
    private Integer almacenId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
}