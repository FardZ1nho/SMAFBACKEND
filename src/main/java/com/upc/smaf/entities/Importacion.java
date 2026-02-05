package com.upc.smaf.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "importaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Importacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codigo_agrupador", unique = true, nullable = false, length = 50)
    private String codigoAgrupador;

    @Enumerated(EnumType.STRING)
    private EstadoImportacion estado = EstadoImportacion.ORDENADO;

    // ✅ CAMPOS QUE FALTABAN (SOLUCIÓN ERROR 2, 3 y 4)
    @Enumerated(EnumType.STRING)
    private TipoTransporte tipoTransporte;

    private LocalDate fechaEstimadaLlegada;
    private LocalDate fechaLlegadaReal;

    // Campos informativos de Aduanas
    private String numeroDua;
    private String canal;
    private String trackingNumber;
    private String agenteAduanas;

    // ==========================================
    // 📊 BASES TOTALES
    // ==========================================
    @Column(precision = 12, scale = 2) private BigDecimal sumaFobTotal = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal pesoTotalKg = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal cbmTotal = BigDecimal.ZERO;

    // ==========================================
    // 💰 GASTOS GLOBALES
    // ==========================================
    // Grupo A: Volumen
    @Column(precision = 12, scale = 2) private BigDecimal costoFlete = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal costoAlmacenajeCft = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal costoTransporteSjl = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal costoPersonalDescarga = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal costoMontacarga = BigDecimal.ZERO;

    // Grupo B: Peso
    @Column(precision = 12, scale = 2) private BigDecimal costoDesconsolidacion = BigDecimal.ZERO;

    // Grupo C: Valor
    @Column(precision = 12, scale = 2) private BigDecimal costoVistosBuenos = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal costoTransmision = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal costoComisionAgencia = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal costoVobo = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal costoGastosOperativos = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal costoResguardo = BigDecimal.ZERO;

    // Impuestos
    @Column(precision = 12, scale = 2) private BigDecimal costoIgv = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal costoIpm = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal costoPercepcion = BigDecimal.ZERO;

    // Otros
    @Column(precision = 12, scale = 2) private BigDecimal costoOtros1 = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal costoOtros2 = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal costoOtros3 = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal costoOtros4 = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2) private BigDecimal costoAdv = BigDecimal.ZERO;

    @OneToMany(mappedBy = "importacion", fetch = FetchType.LAZY)
    private List<Compra> facturas = new ArrayList<>();

    private LocalDateTime fechaCreacion;

    @PrePersist
    void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        if(sumaFobTotal == null) sumaFobTotal = BigDecimal.ZERO;
        if(pesoTotalKg == null) pesoTotalKg = BigDecimal.ZERO;
        if(cbmTotal == null) cbmTotal = BigDecimal.ZERO;

        if(costoFlete == null) costoFlete = BigDecimal.ZERO;
        // ... (resto de inicializaciones opcionales)
    }
}