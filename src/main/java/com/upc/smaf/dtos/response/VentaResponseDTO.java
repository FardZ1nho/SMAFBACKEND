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

    // Campos financieros
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

    // Lista de productos
    private List<DetalleVentaResponseDTO> detalles;

    // ✅ CORRECCIÓN CRÍTICA: Descomentamos la lista de pagos para enviarla a Angular
    private List<PagoResponseDTO> pagos;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // ✅ Clase interna para estructurar el pago en la respuesta
    @Data
    public static class PagoResponseDTO {
        private Integer id;
        private BigDecimal monto;
        private String moneda;
        private MetodoPago metodoPago;
        private String fechaPago;
        private String referencia;
        private String nombreCuentaDestino; // Opcional, si quieres mostrar banco
    }
}