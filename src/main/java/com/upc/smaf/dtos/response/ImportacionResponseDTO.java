package com.upc.smaf.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ImportacionResponseDTO {
    private Integer id; // ✅ CORREGIDO: Integer
    private String codigoAgrupador;
    private String estado;
    private String tipoTransporte;

    private LocalDate fechaEstimadaLlegada;
    private LocalDate fechaLlegadaReal;

    private String numeroDua;
    private String canal;
    private String trackingNumber;
    private String agenteAduanas;

    private BigDecimal totalFleteInternacional;
    private BigDecimal totalSeguro;
    private BigDecimal totalGastosAduana;
    private BigDecimal totalGastosAlmacen;
    private BigDecimal totalTransporteLocal;
    private BigDecimal otrosGastosGlobales;

    private BigDecimal sumaFobTotal;
    private BigDecimal pesoTotalKg;

    private List<CompraResumenDTO> facturasComerciales;

    @Data
    public static class CompraResumenDTO {
        private Integer id; // ✅ CORREGIDO: Integer
        private String serie;
        private String numero;
        private String nombreProveedor;
        private BigDecimal total;
        private String moneda;
        private BigDecimal pesoNetoKg;
    }
}