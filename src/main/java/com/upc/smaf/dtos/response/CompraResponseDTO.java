package com.upc.smaf.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List; // Importante

@Data
public class CompraResponseDTO {
    private Integer id;
    private String serie;
    private String numero;
    private String tipoCompra;
    private String tipoPago;
    private LocalDate fechaEmision;

    // Proveedor
    private String nombreProveedor;
    private String rucProveedor;

    // Totales
    private String moneda;
    private BigDecimal tipoCambio;
    private BigDecimal subTotal;
    private BigDecimal fob;
    private BigDecimal igv;
    private BigDecimal total;
    private BigDecimal saldoPendiente;

    private String estado;

    // Campos de Importación
    private String codImportacion;
    private BigDecimal pesoNetoKg;
    private BigDecimal cbm;

    // Prorrateo
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

    // ✅ USAMOS TU CLASE EXISTENTE AQUÍ
    private List<CompraDetalleResponseDTO> detalles;
}