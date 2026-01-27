package com.upc.smaf.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductoResponseDTO {
    private Integer id;
    private String tipo; // "PRODUCTO" or "SERVICIO"
    private String nombre;
    private String codigo;
    private String descripcion;

    private Integer idCategoria;
    private String nombreCategoria;

    private Integer stockActual; // Physical stock in warehouse
    private Integer stockMinimo;

    // ✅ NEW FIELD: Stock in transit (Ordered/In Transit/Customs)
    private Integer stockPorLlegar;

    private BigDecimal precioChina;
    private BigDecimal costoTotal;
    private BigDecimal precioVenta;

    private String moneda;
    private String unidadMedida;

    private Boolean activo;
    private LocalDateTime fechaCreacion;

    // Calculated fields
    private BigDecimal margenGanancia;
    private Double porcentajeMargen;
    private String estadoStock;
    private Boolean necesitaReorden;
}