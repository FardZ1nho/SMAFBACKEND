package com.upc.smaf.dtos.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map; // ✅ Importante para recibir el mapa de Ad Valorem

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

    // --- IMPUESTOS ---
    private BigDecimal costoIgv;
    private BigDecimal costoIpm;
    private BigDecimal costoPercepcion;

    // ⚠️ EL AD VALOREM YA NO ES UN INPUT ÚNICO, AHORA ES UN MAPA
    // Key: ID de la Factura (Compra), Value: Monto Manual
    private Map<Integer, BigDecimal> adValoremPorFactura;

    // (Opcional) Puedes dejar este campo si quieres enviar la suma total desde el front,
    // pero el backend la recalculará sumando el mapa.
    private BigDecimal costoAdv;

    // --- OTROS ---
    private BigDecimal costoOtros1;
    private BigDecimal costoOtros2;
    private BigDecimal costoOtros3;
    private BigDecimal costoOtros4;
}