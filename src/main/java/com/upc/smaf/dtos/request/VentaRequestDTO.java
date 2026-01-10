package com.upc.smaf.dtos.request;

import com.upc.smaf.entities.MetodoPago;
import com.upc.smaf.entities.TipoCliente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VentaRequestDTO {

    private LocalDateTime fechaVenta;

    @Size(max = 100, message = "El nombre del cliente no puede exceder 100 caracteres")
    private String nombreCliente;

    @NotNull(message = "El tipo de cliente es requerido")
    private TipoCliente tipoCliente;

    @NotNull(message = "El método de pago es requerido")
    private MetodoPago metodoPago;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notas;

    @NotEmpty(message = "Debe agregar al menos un producto a la venta")
    @Valid
    private List<DetalleVentaRequestDTO> detalles;
}