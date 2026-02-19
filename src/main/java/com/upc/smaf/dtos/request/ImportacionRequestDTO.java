package com.upc.smaf.dtos.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
public class ImportacionRequestDTO {
    // Datos Básicos
    private String codigoAgrupador;
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

    private BigDecimal costoFlete;
    private BigDecimal costoAlmacenajeCft;
    private BigDecimal costoTransporteSjl;
    private BigDecimal costoPersonalDescarga;
    private BigDecimal costoMontacarga;

    private BigDecimal costoDesconsolidacion;

    private BigDecimal costoVistosBuenos;
    private BigDecimal costoTransmision;
    private BigDecimal costoComisionAgencia;
    private BigDecimal costoVobo;
    private BigDecimal costoGastosOperativos;
    private BigDecimal costoResguardo;

    private BigDecimal costoIgv;
    private BigDecimal costoIpm;
    private BigDecimal costoPercepcion;

    // ✅ MODIFICACIÓN CLAVE: MAPA DE AD VALOREM POR ÍTEM
    // Key: ID del CompraDetalle, Value: Monto Total de Ad Valorem para ese ítem
    private Map<Integer, BigDecimal> adValoremPorItem;

    private BigDecimal costoAdv; // Opcional, lo recalcula el backend.

    private BigDecimal costoOtros1;
    private BigDecimal costoOtros2;
    private BigDecimal costoOtros3;
    private BigDecimal costoOtros4;
}