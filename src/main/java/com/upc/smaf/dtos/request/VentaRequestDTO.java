package com.upc.smaf.dtos.request;

import com.upc.smaf.entities.MetodoPago;
import com.upc.smaf.entities.TipoCliente;
import com.upc.smaf.entities.TipoPago;
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

    @NotNull(message = "El tipo de pago es requerido")
    private TipoPago tipoPago;

    @NotNull(message = "El método de pago es requerido")
    private MetodoPago metodoPago;

    // ✅ NUEVO: Cuenta destino (Yape/Plin/Banco)
    // Es opcional porque si es EFECTIVO no se envía.
    private Integer cuentaBancariaId;

    private BigDecimal pagoEfectivo;
    private BigDecimal pagoTransferencia;

    // Campos de Crédito
    private BigDecimal montoInicial;
    private Integer numeroCuotas;

    private String moneda;
    private BigDecimal tipoCambio;

    private String tipoDocumento;
    private String numeroDocumento;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notas;

    @NotEmpty(message = "Debe agregar al menos un producto a la venta")
    @Valid
    private List<DetalleVentaRequestDTO> detalles;
}