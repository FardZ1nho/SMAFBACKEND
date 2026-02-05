package com.upc.smaf.dtos.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ImportacionRequestDTO {
    // Datos Básicos
    private String codigoAgrupador; // Opcional si solo se edita por ID
    private String estado;
    private String tipoTransporte;
    private LocalDate fechaEstimadaLlegada;
    private LocalDate fechaLlegadaReal;

    // Aduanas
    private String numeroDua;
    private String trackingNumber;
    private String agenteAduanas;
    private String canal;

    // ==========================================
    // 💰 GASTOS GLOBALES (INPUTS DEL USUARIO)
    // ==========================================

    // --- GRUPO A: VOLUMEN (CBM) ---
    private BigDecimal costoFlete;
    private BigDecimal costoAlmacenajeCft;
    private BigDecimal costoTransporteSjl;
    private BigDecimal costoPersonalDescarga;
    private BigDecimal costoMontacarga;

    // --- GRUPO B: PESO (KG) ---
    private BigDecimal costoDesconsolidacion;

    // --- GRUPO C: VALOR (FOB) ---
    private BigDecimal costoVistosBuenos;
    private BigDecimal costoTransmision;
    private BigDecimal costoComisionAgencia;
    private BigDecimal costoVobo; // VºBº
    private BigDecimal costoGastosOperativos;
    private BigDecimal costoResguardo;

    // Impuestos
    private BigDecimal costoIgv;
    private BigDecimal costoIpm;
    private BigDecimal costoPercepcion;

    // Otros
    private BigDecimal costoOtros1;
    private BigDecimal costoOtros2;
    private BigDecimal costoOtros3;
    private BigDecimal costoOtros4;
    private BigDecimal costoAdv;
}