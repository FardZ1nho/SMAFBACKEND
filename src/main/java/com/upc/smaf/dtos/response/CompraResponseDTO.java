package com.upc.smaf.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompraResponseDTO {
    private Integer id;

    // Datos Principales
    private String tipoCompra;      // BIEN o SERVICIO
    private String tipoComprobante; // Factura, Boleta...
    private String serie;
    private String numero;

    // Fechas
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private LocalDateTime fechaRegistro;

    // Proveedor
    private String nombreProveedor;
    private String rucProveedor;

    // Moneda
    private String moneda;
    private BigDecimal tipoCambio;

    // ✅ TOTALES Y MONTOS (BigDecimal)
    private BigDecimal subTotal;
    private BigDecimal igv;
    private BigDecimal total;

    // ✅ IMPUESTOS ESPECÍFICOS
    private BigDecimal percepcion;
    private BigDecimal detraccionPorcentaje;
    private BigDecimal detraccionMonto;
    private BigDecimal retencion;

    private String observaciones;
    private String estado; // Activo/Anulado

    // Detalles
    private List<CompraDetalleResponseDTO> detalles;
}