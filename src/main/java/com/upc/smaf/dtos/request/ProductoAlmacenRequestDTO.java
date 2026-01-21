package com.upc.smaf.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoAlmacenRequestDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    private Integer productoId;

    @NotNull(message = "El ID del almacén es obligatorio")
    private Long almacenId;

    // ✅ CORREGIDO: Se llama 'cantidad' para que coincida con el Servicio
    // Representa cuánto vas a SUMAR, no el total final.
    @NotNull(message = "La cantidad a ingresar es obligatoria")
    @Min(value = 1, message = "Debes ingresar al menos 1 unidad")
    private Integer cantidad;

    @Size(max = 100, message = "La ubicación física no puede tener más de 100 caracteres")
    private String ubicacionFisica;

    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;

    private Boolean activo;
}