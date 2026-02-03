package com.upc.smaf.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompraResponseDTO {
    // CAMBIO: Long -> Integer
    private Integer id;
    private String tipoCompra;
    private String tipoComprobante;
    private String tipoPago;
    private String estado;

    private String serie;
    private String numero;

    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private LocalDateTime fechaRegistro;

    private String nombreProveedor;
    private String rucProveedor;

    private String moneda;
    private BigDecimal tipoCambio;

    private BigDecimal subTotal;
    private BigDecimal igv;
    private BigDecimal total;

    private BigDecimal saldoPendiente;
    private BigDecimal montoPagado;

    private BigDecimal percepcion;
    private BigDecimal detraccionPorcentaje;
    private BigDecimal detraccionMonto;
    private BigDecimal retencion;

    // Importación
    private String codImportacion;
    private BigDecimal pesoNetoKg;
    private Integer bultos;
    // CAMBIO: Long -> Integer
    private Integer importacionId;

    private BigDecimal costoTotalImportacion;
    private BigDecimal prorrateoFlete;
    private BigDecimal prorrateoSeguro;
    private BigDecimal prorrateoGastosAduanas;

    private List<CompraDetalleResponseDTO> detalles;
    private List<PagoCompraResponseDTO> pagos;
}