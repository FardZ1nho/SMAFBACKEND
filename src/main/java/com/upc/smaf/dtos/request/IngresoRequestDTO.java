package com.upc.smaf.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngresoRequestDTO {
    private Integer productoId;
    private Integer cantidad;
    private String proveedor;
    private String observacion;
    private LocalDateTime fecha;

    // ✅ NUEVO: ID del almacén donde se ingresa el stock
    private Long almacenId;
}