
package com.upc.smaf.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductoResponseDTO {
    private Integer id;
    private String nombre;
    private String codigo;

    // 👇 AGREGA ESTO
    private String tipo;

    private String descripcion;
    private Integer idCategoria;
    private String nombreCategoria;
    private Integer stockActual;
    private Integer stockMinimo;
    private BigDecimal precioChina;
    private BigDecimal costoTotal;
    private BigDecimal precioVenta;
    private String moneda;
    private String unidadMedida;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private String estadoStock;
    private Boolean necesitaReorden;
    private BigDecimal margenGanancia;
    private Double porcentajeMargen;
}