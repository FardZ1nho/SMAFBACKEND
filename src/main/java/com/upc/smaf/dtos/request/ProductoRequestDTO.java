package com.upc.smaf.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductoRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String codigo;
    private String ubicacionAlmacen;
    private String descripcion;

    @NotNull(message = "La categoría es obligatoria")
    private Integer idCategoria;

    @NotNull(message = "El stock actual es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stockActual = 0;

    @NotNull(message = "El stock mínimo es obligatorio")
    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo = 5;

    // ⭐⭐⭐ TRES PRECIOS ⭐⭐⭐
    private BigDecimal precioChina;      // Precio en origen
    private BigDecimal costoTotal;       // Costo real (China + envío + impuestos)
    private BigDecimal precioVenta;      // Precio de venta

    // ⭐⭐⭐ MONEDA ÚNICA ⭐⭐⭐
    private String moneda = "USD";       // USD, PEN, EUR, etc.

    private String unidadMedida = "unidad";
}