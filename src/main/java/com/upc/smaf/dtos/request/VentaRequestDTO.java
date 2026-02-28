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

    @NotEmpty(message = "Debe registrar al menos un método de pago")
    @Valid
    private List<PagoRequestDTO> pagos;

    private Integer numeroCuotas;

    private String moneda;
    private BigDecimal tipoCambio;

    private String tipoDocumento;
    private String numeroDocumento;

    // --- NUEVO: RETENCIÓN Y DETRACCIÓN ---
    @PositiveOrZero(message = "La retención no puede ser negativa")
    private BigDecimal retencion;

    @PositiveOrZero(message = "La detracción no puede ser negativa")
    private BigDecimal detraccion;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notas;

    @NotEmpty(message = "Debe agregar al menos un producto a la venta")
    @Valid
    private List<DetalleVentaRequestDTO> detalles;

    @Data
    public static class PagoRequestDTO {
        @NotNull(message = "El método de pago es requerido")
        private MetodoPago metodoPago;

        @NotNull
        @Positive(message = "El monto debe ser mayor a 0")
        private BigDecimal monto;

        @NotNull
        private String moneda;

        private Integer cuentaBancariaId;
        private String referencia;
    }
}