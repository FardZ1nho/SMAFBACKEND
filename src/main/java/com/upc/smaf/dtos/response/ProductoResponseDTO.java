package com.upc.smaf.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductoResponseDTO {

    private Integer id;
    private String nombre;
    private String codigo;
    private String descripcion;

    private Integer idCategoria;
    private String nombreCategoria;

    private Integer stockActual;
    private Integer stockMinimo;

    // ⭐⭐⭐ TRES PRECIOS ⭐⭐⭐
    private BigDecimal precioChina;      // Precio en origen
    private BigDecimal costoTotal;       // Costo real total
    private BigDecimal precioVenta;      // Precio de venta

    // ⭐⭐⭐ MONEDA ⭐⭐⭐
    private String moneda;               // USD, PEN, EUR

    private String unidadMedida;
    private String ubicacionAlmacen;
    private Boolean activo;
    private LocalDateTime fechaCreacion;

    // Campos calculados
    private String estadoStock;
    private Boolean necesitaReorden;
    private BigDecimal margenGanancia;   // ⭐ NUEVO: precioVenta - costoTotal
    private Double porcentajeMargen;     // ⭐ NUEVO: (margen / precioVenta) * 100
}