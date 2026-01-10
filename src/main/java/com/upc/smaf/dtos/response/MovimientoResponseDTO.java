package com.upc.smaf.dtos.response;

import com.upc.smaf.entities.Movimiento.TipoMovimiento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoResponseDTO {

    private Long id;
    private String codigo;

    // Producto
    private Integer productoId;
    private String productoNombre;
    private String productoCodigo;

    // Almacenes
    private Long almacenOrigenId;
    private String almacenOrigenNombre;
    private Long almacenDestinoId;
    private String almacenDestinoNombre;

    // Detalles del movimiento
    private TipoMovimiento tipoMovimiento;
    private String tipoMovimientoLabel; // "Traslado", "Entrada", etc.
    private Integer cantidad;
    private String motivo;
    private String usuarioResponsable;

    // Fechas
    private LocalDateTime fechaMovimiento;
    private LocalDateTime fechaCreacion;
}