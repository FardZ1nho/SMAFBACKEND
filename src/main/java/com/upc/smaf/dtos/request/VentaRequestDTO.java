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
    private TipoPago tipoPago; // CONTADO o CREDITO

    // --- NUEVO: LISTA DINÁMICA DE PAGOS ---
    // Ya no usamos campos fijos como pagoEfectivo o cuentaBancariaId aquí.
    @NotEmpty(message = "Debe registrar al menos un método de pago")
    @Valid
    private List<PagoRequestDTO> pagos;

    // Campos de Crédito (Solo si es a crédito)
    private Integer numeroCuotas;

    private String moneda; // Moneda del documento (PEN o USD)
    private BigDecimal tipoCambio;

    private String tipoDocumento;
    private String numeroDocumento;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notas;

    @NotEmpty(message = "Debe agregar al menos un producto a la venta")
    @Valid
    private List<DetalleVentaRequestDTO> detalles;

    // --- CLASE INTERNA PARA DEFINIR CADA PAGO ---
    @Data
    public static class PagoRequestDTO {
        @NotNull(message = "El método de pago es requerido")
        private MetodoPago metodoPago; // EFECTIVO, TRANSFERENCIA, YAPE, ETC.

        @NotNull
        @Positive(message = "El monto debe ser mayor a 0")
        private BigDecimal monto;

        @NotNull
        private String moneda; // 'PEN' o 'USD' (Puede ser diferente a la venta)

        private Integer cuentaBancariaId; // Opcional (Solo para bancos/digitales)
        private String referencia; // Opcional (Nro operación)
    }
}