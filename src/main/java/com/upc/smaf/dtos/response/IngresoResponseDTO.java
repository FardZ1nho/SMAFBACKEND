package com.upc.smaf.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngresoResponseDTO {
    private Integer id;
    private String nombreProducto;
    private String skuProducto;
    private Integer cantidad;
    private LocalDateTime fecha;
    private String proveedor;

    // ✅ NUEVO: Información del almacén
    private Long almacenId;
    private String almacenCodigo;
    private String almacenNombre;
}