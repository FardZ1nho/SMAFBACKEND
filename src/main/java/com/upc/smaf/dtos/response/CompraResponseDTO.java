package com.upc.smaf.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CompraResponseDTO {
    private Integer id;
    private String serie;
    private String numero;
    private String tipoCompra; // BIEN o SERVICIO
    private String tipoPago;   // CONTADO o CREDITO
    private LocalDate fechaEmision;

    // Proveedor
    private String nombreProveedor;
    private String rucProveedor;

    // Totales
    private String moneda;
    private BigDecimal tipoCambio;
    private BigDecimal subTotal;
    private BigDecimal fob; // Adicional
    private BigDecimal igv;
    private BigDecimal total;
    private BigDecimal saldoPendiente; // Calculado

    private String estado; // PENDIENTE, COMPLETADA, ANULADA

    // ✅ CAMPOS DE IMPORTACIÓN QUE FALTABAN
    private String codImportacion;
    private BigDecimal pesoNetoKg;
    private BigDecimal cbm;

    // ✅ RESULTADOS PRORRATEO (Para ver detalle en la compra)
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
}