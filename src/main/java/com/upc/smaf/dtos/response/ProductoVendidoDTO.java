// src/main/java/com/upc/smaf/dtos/response/ProductoVendidoDTO.java

package com.upc.smaf.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoVendidoDTO {
    private Integer id;
    private String nombre;
    private String codigo;
    private Long cantidadVendida;
    private BigDecimal totalVentas;
}