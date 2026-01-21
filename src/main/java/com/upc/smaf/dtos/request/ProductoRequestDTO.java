package com.upc.smaf.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductoRequestDTO {

    // 👇 NUEVO CAMPO (Puede ser "PRODUCTO" o "SERVICIO")
    private String tipo;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String codigo;
    private String descripcion;

    @NotNull(message = "La categoría es obligatoria")
    private Integer idCategoria;

    // Para Servicios, mandaremos 0 desde el frontend
    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo = 0;

    private BigDecimal precioChina;
    private BigDecimal costoTotal;
    private BigDecimal precioVenta;

    private String moneda = "USD";
    private String unidadMedida = "unidad";
}