package com.upc.smaf.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoVendidoDTO {
    private Integer id;           // ✅ Nuevo campo requerido por tu repositorio
    private String nombreProducto;
    private String codigo;        // ✅ Nuevo campo requerido por tu repositorio
    private Long cantidad;
    private BigDecimal total;
}