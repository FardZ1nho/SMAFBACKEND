package com.upc.smaf.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrasladoRequestDTO {

    @NotNull(message = "El producto es obligatorio")
    private Integer productoId;

    @NotNull(message = "El almacén de origen es obligatorio")
    private Long almacenOrigenId;

    @NotNull(message = "El almacén de destino es obligatorio")
    private Long almacenDestinoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    private String motivo;
}