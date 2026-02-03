package com.upc.smaf.entities;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "importaciones")
public class Importacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ✅ CORREGIDO: Integer (Coincide con tu BD)

    @Column(nullable = false, unique = true)
    private String codigoAgrupador; // Ej: CHINA-2026-01

    @Enumerated(EnumType.STRING)
    private EstadoImportacion estado;

    @Enumerated(EnumType.STRING)
    private TipoTransporte tipoTransporte;

    private LocalDate fechaEstimadaLlegada;
    private LocalDate fechaLlegadaReal;

    // --- CAMPOS NUEVOS (ADUANAS) ---
    private String numeroDua;
    private String canal;          // VERDE, ROJO, NARANJA
    private String trackingNumber; // BL o Tracking
    private String agenteAduanas;

    // --- COSTOS GLOBALES (Para Prorrateo) ---
    private BigDecimal totalFleteInternacional;
    private BigDecimal totalSeguro;
    private BigDecimal totalGastosAduana;
    private BigDecimal totalGastosAlmacen;
    private BigDecimal totalTransporteLocal;
    private BigDecimal otrosGastosGlobales;

    // --- TOTALES CALCULADOS ---
    private BigDecimal sumaFobTotal;
    private BigDecimal pesoTotalKg;
}