package com.upc.smaf.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List; // Importante

@Data
public class ProductoRequestDTO {

    private String tipo; // "PRODUCTO", "SERVICIO", "KIT"

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String codigo;
    private String codigoInternacional;
    private String descripcion;

    @NotNull(message = "La categoría es obligatoria")
    private Integer idCategoria;

    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo = 0;

    private BigDecimal precioChina;
    private BigDecimal costoTotal;
    private BigDecimal precioVenta;

    private String moneda = "USD";
    private String unidadMedida = "unidad";

    // ✅ NUEVO: Lista de componentes (Solo se usa si tipo="KIT")
    private List<ComponenteDTO> componentes;

    // ✅ CLASE INTERNA para definir qué lleva el Kit
    @Data
    public static class ComponenteDTO {
        private Integer idProducto; // ID del producto hijo (ej. Tubo)
        private Integer cantidad;   // Cuántos lleva (ej. 3)
    }
}