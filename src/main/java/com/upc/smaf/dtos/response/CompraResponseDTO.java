package com.upc.smaf.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CompraResponseDTO {
    private Integer id;
    private String serie;
    private String numero;

    // ✅ AGREGA ESTA LÍNEA AQUÍ
    private String tipoComprobante;

    private String tipoCompra;
    private String tipoPago;
    private LocalDate fechaEmision;
    private String estado;

    private String nombreProveedor;
    private String rucProveedor;

    private String moneda;
    private BigDecimal tipoCambio;
    private BigDecimal subTotal;
    private BigDecimal fob;
    private BigDecimal igv;
    private BigDecimal total;
    private BigDecimal saldoPendiente;

    // Datos Logísticos (Importación)
    private String codImportacion;
    private BigDecimal pesoNetoKg;
    private BigDecimal cbm;

    // Prorrateo Landed Cost
    private BigDecimal proFlete;
    private BigDecimal proAlmacenaje;
    private BigDecimal proTransporte;
    private BigDecimal proCargaDescarga;
    private BigDecimal proDesconsolidacion;
    private BigDecimal proGastosAduaneros;
    private BigDecimal proSeguroResguardo;
    private BigDecimal proImpuestos;
    private BigDecimal proOtrosGastos;
    private BigDecimal costoTotalImportacion;

    private List<CompraDetalleResponseDTO> detalles;
}