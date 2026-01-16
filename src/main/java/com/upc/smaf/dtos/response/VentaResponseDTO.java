package com.upc.smaf.dtos.response;

import com.upc.smaf.entities.EstadoVenta;
import com.upc.smaf.entities.MetodoPago;
import com.upc.smaf.entities.TipoCliente;
import com.upc.smaf.entities.TipoPago;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VentaResponseDTO {

    private Integer id;
    private String codigo;
    private LocalDateTime fechaVenta;
    private String nombreCliente;
    private TipoCliente tipoCliente;

    private TipoPago tipoPago;
    private MetodoPago metodoPago;

    // ✅ NUEVO: Información de la cuenta destino
    private Integer cuentaBancariaId;
    private String nombreCuentaBancaria; // Ej: "Yape Patrick"

    private BigDecimal pagoEfectivo;
    private BigDecimal pagoTransferencia;

    private BigDecimal montoInicial;
    private Integer numeroCuotas;
    private BigDecimal montoCuota;
    private BigDecimal saldoPendiente;

    private String moneda;
    private BigDecimal tipoCambio;

    private String tipoDocumento;
    private String numeroDocumento;

    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private String notas;
    private EstadoVenta estado;

    // Lista de detalles
    private List<DetalleVentaResponseDTO> detalles;

    // Lista de pagos (historial de amortizaciones)
    // Es útil incluir esto si quieres ver el historial completo en el detalle
    // private List<PagoResponseDTO> pagos;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}