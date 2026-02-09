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
    private String trackingNumber;
    private String agenteAduanas;
    private String canal;

    // TOTALES CABECERA
    private BigDecimal sumaFobTotal;
    private BigDecimal pesoTotalKg;
    private BigDecimal cbmTotal;

    // COSTOS GLOBALES (Inputs)
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
    private BigDecimal costoAdv; // Suma total informativa

    private BigDecimal costoOtros1;
    private BigDecimal costoOtros2;
    private BigDecimal costoOtros3;
    private BigDecimal costoOtros4;

    // LISTA DE FACTURAS
    private List<CompraResumenDTO> facturasComerciales;

    // ==========================================
    // 📄 DTO DE LA FACTURA (NIVEL 1)
    // ==========================================
    @Data
    public static class CompraResumenDTO {
        private Integer id;
        private String serie;
        private String numero;
        private String nombreProveedor;
        private BigDecimal total; // FOB Total Factura
        private String moneda;
        private BigDecimal pesoNetoKg;
        private BigDecimal cbm;

        // --- DETALLE PRORRATEADO COMPLETO (NIVEL 1) ---
        // Estos campos deben coincidir con la Entidad Compra actualizada

        // Grupo Volumen
        private BigDecimal proFlete;
        private BigDecimal proAlmacenaje;
        private BigDecimal proTransporte;
        private BigDecimal proPersonalDescarga; // Nuevo
        private BigDecimal proMontacarga;       // Nuevo

        // Grupo Peso
        private BigDecimal proDesconsolidacion;

        // Grupo Valor / Aduanas
        private BigDecimal proVistosBuenos;     // Nuevo
        private BigDecimal proTransmision;      // Nuevo
        private BigDecimal proComisionAgencia;  // Nuevo
        private BigDecimal proVobo;             // Nuevo
        private BigDecimal proGastosOperativos; // Nuevo
        private BigDecimal proResguardo;        // Nuevo

        // Grupo Impuestos
        private BigDecimal proAdv;
        private BigDecimal proIgv;              // Nuevo
        private BigDecimal proIpm;              // Nuevo
        private BigDecimal proPercepcion;       // Nuevo

        // Grupo Otros
        private BigDecimal proOtros1;           // Nuevo
        private BigDecimal proOtros2;           // Nuevo
        private BigDecimal proOtros3;           // Nuevo
        private BigDecimal proOtros4;           // Nuevo

        private BigDecimal costoTotalImportacion; // Landed Cost Factura

        // LISTA DE ÍTEMS
        private List<DetalleItemDTO> items;
    }

    // ==========================================
    // 📦 DTO DEL ÍTEM / PRODUCTO (NIVEL 2)
    // ==========================================
    @Data
    public static class DetalleItemDTO {
        private String nombreProducto;
        private BigDecimal cantidad;
        private BigDecimal precioUnitarioFob;
        private BigDecimal importeFob; // Total FOB del ítem

        private BigDecimal factorParticipacion; // % respecto a la factura

        // ✅ DESGLOSE DE COSTOS UNITARIOS (CALCULADO)
        // Estos campos ahora existen para que la tabla de items tenga todas las columnas

        private BigDecimal itemFlete;
        private BigDecimal itemAlmacenaje;
        private BigDecimal itemTransporte;
        private BigDecimal itemDescarga;
        private BigDecimal itemMontacarga;

        private BigDecimal itemDesconsolidacion;

        private BigDecimal itemVistosBuenos;
        private BigDecimal itemTransmision;
        private BigDecimal itemAgente;
        private BigDecimal itemVobo;
        private BigDecimal itemGastosOp;
        private BigDecimal itemResguardo;

        private BigDecimal itemAdv;
        private BigDecimal itemIgv;
        private BigDecimal itemIpm;
        private BigDecimal itemPercepcion;

        private BigDecimal itemOtros1;
        private BigDecimal itemOtros2;

        private BigDecimal costoUnitarioLanded; // Costo Final Unitario
        private BigDecimal costoTotalLanded;    // Costo Final Total
    }
}