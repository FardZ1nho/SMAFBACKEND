package com.upc.smaf.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompraResponseDTO {
    private Integer id;

    private String tipoCompra;
    private String tipoComprobante;
    private String tipoPago; // CONTADO / CREDITO
    private String estado;

    private String serie;
    private String numero;

    // ✅ NUEVO: ID DE IMPORTACIÓN (Para mostrarlo en el detalle o lista)
    private String codImportacion;

    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private LocalDateTime fechaRegistro;

    private String nombreProveedor;
    private String rucProveedor;

    private String moneda;
    private BigDecimal tipoCambio;

    // Totales
    private BigDecimal subTotal;
    private BigDecimal igv;
    private BigDecimal total;

    // ✅ SALDOS
    private BigDecimal montoPagadoInicial;
    private BigDecimal saldoPendiente;

    // Impuestos
    private BigDecimal percepcion;
    private BigDecimal detraccionPorcentaje;
    private BigDecimal detraccionMonto;
    private BigDecimal retencion;

    private String observaciones;

    private List<CompraDetalleResponseDTO> detalles;

    // ✅ LISTA DE PAGOS REALIZADOS
    private List<PagoCompraResponseDTO> pagos;

    @Data
    public static class PagoCompraResponseDTO {
        private Integer id;
        private BigDecimal monto;
        private String moneda;
        private String metodoPago;
        private String fechaPago;
        private String referencia;
        private String nombreCuentaOrigen;
    }
}