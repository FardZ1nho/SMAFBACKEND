package com.upc.smaf.dtos.request;

import com.upc.smaf.entities.Movimiento.TipoMovimiento;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoRequestDTO {

    @NotNull(message = "El producto es obligatorio")
    private Integer productoId;

    private Long almacenOrigenId;  // Null si es ENTRADA o AJUSTE_POSITIVO

    private Long almacenDestinoId; // Null si es SALIDA o AJUSTE_NEGATIVO

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private TipoMovimiento tipoMovimiento;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    private String motivo;

    private String usuarioResponsable;

    private LocalDateTime fechaMovimiento; // Opcional, si no se envía, se usa la fecha actual
}