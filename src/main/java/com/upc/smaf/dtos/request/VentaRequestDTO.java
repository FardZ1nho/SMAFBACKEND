package com.upc.smaf.dtos.request;

import com.upc.smaf.entities.MetodoPago;
import com.upc.smaf.entities.TipoCliente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VentaRequestDTO {
    private LocalDateTime fechaVenta;
    private Integer clienteId;
    private String nombreCliente;

    @NotNull(message = "El tipo de cliente es requerido")
    private TipoCliente tipoCliente;

    @NotNull(message = "El método de pago es requerido")
    private MetodoPago metodoPago;

    // ✅ NUEVO: Campos para recibir los montos del Pago Mixto
    private BigDecimal pagoEfectivo;
    private BigDecimal pagoTransferencia;

    // Para saber en qué moneda se hizo la transacción
    private String moneda;

    // Para guardar el TC del momento
    private BigDecimal tipoCambio;

    private String tipoDocumento;
    private String numeroDocumento;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notas;

    @NotEmpty(message = "Debe agregar al menos un producto a la venta")
    @Valid
    private List<DetalleVentaRequestDTO> detalles;
}