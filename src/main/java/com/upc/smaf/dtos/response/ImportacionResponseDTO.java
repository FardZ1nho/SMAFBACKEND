package com.upc.smaf.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ImportacionResponseDTO {
    private Integer id;
    private String codigoAgrupador;
    private String estado;
    private String tipoTransporte;

    private LocalDate fechaEstimadaLlegada;
    private LocalDate fechaLlegadaReal;

    private String numeroDua;
    private String canal;
    private String trackingNumber;
    private String agenteAduanas;

    // ==========================================
    // 📊 TOTALES DE LA CARPETA (Base de cálculo)
    // ==========================================
    private BigDecimal sumaFobTotal;
    private BigDecimal pesoTotalKg;
    private BigDecimal cbmTotal; // ✅ Volumen Total

    // ==========================================
    // 💰 GASTOS GLOBALES (Inputs del Usuario)
    // ==========================================

    // Grupo Volumen
    private BigDecimal costoFlete;
    private BigDecimal costoAlmacenajeCft;
    private BigDecimal costoTransporteSjl;
    private BigDecimal costoPersonalDescarga;
    private BigDecimal costoMontacarga;

    // Grupo Peso
    private BigDecimal costoDesconsolidacion;

    // Grupo Valor
    private BigDecimal costoVistosBuenos;
    private BigDecimal costoTransmision;
    private BigDecimal costoComisionAgencia;
    private BigDecimal costoVobo;
    private BigDecimal costoGastosOperativos;
    private BigDecimal costoResguardo;

    // Impuestos
    private BigDecimal costoIgv;
    private BigDecimal costoIpm;
    private BigDecimal costoPercepcion;
    private BigDecimal costoAdv;

    // Otros
    private BigDecimal costoOtros1;
    private BigDecimal costoOtros2;
    private BigDecimal costoOtros3;
    private BigDecimal costoOtros4;

    // Lista de Facturas
    private List<CompraResumenDTO> facturasComerciales;

    // ==========================================
    // 📄 CLASE INTERNA: RESUMEN DE FACTURA
    // ==========================================
    @Data
    public static class CompraResumenDTO {
        private Integer id;
        private String serie;
        private String numero;
        private String nombreProveedor;
        private BigDecimal total; // FOB
        private String moneda;
        private BigDecimal pesoNetoKg;
        private BigDecimal cbm;

        // ==========================================
        // 📊 DETALLE FULL (1 a 1 con los inputs)
        // ==========================================

        // --- GRUPO VOLUMEN ---
        private BigDecimal proFlete;
        private BigDecimal proAlmacenaje;
        private BigDecimal proTransporte;
        private BigDecimal proPersonalDescarga; // Desagrupado
        private BigDecimal proMontacarga;       // Desagrupado

        // --- GRUPO PESO ---
        private BigDecimal proDesconsolidacion;

        // --- GRUPO VALOR ---
        private BigDecimal proVistosBuenos;     // Desagrupado
        private BigDecimal proTransmision;      // Desagrupado
        private BigDecimal proComisionAgencia;  // Desagrupado
        private BigDecimal proVobo;             // Desagrupado
        private BigDecimal proGastosOperativos; // Desagrupado
        private BigDecimal proResguardo;

        // --- IMPUESTOS ---
        private BigDecimal proAdv;              // Desagrupado
        private BigDecimal proIgv;              // Desagrupado
        private BigDecimal proIpm;              // Desagrupado
        private BigDecimal proPercepcion;       // Desagrupado

        // --- OTROS ---
        private BigDecimal proOtros1;
        private BigDecimal proOtros2;
        private BigDecimal proOtros3;
        private BigDecimal proOtros4;

        // Costo Final
        private BigDecimal costoTotalImportacion;
    }
}