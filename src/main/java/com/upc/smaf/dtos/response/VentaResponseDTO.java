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
    private EstadoVenta estado;
    private TipoPago tipoPago;

    private String tipoDocumento;
    private String numeroDocumento;
    private String moneda;

    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;

    // ✅ CAMPOS DE IMPUESTOS Y TOTAL NETO (Calculado en Backend)
    private BigDecimal retencion;
    private BigDecimal detraccion;
    private BigDecimal totalNeto;

    private BigDecimal saldoPendiente;
    private String notas;

    private List<DetalleVentaResponseDTO> detalles;
    private List<PagoResponseDTO> pagos;

    @Data
    public static class PagoResponseDTO {
        private Integer id;
        private BigDecimal monto;
        private String moneda;
        private MetodoPago metodoPago;
        private String fechaPago;
        private String referencia;
        private String nombreCuentaDestino;
    }
}