package com.upc.smaf.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compras")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_compra", nullable = false, length = 20)
    private TipoCompra tipoCompra;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprobante", nullable = false, length = 30)
    private TipoComprobante tipoComprobante;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago", nullable = false)
    private TipoPago tipoPago;

    @Column(nullable = false, length = 10)
    private String serie;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(name = "fec_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fec_vencimiento")
    private LocalDate fechaVencimiento;

    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(length = 10)
    private String moneda = "PEN";

    @Column(name = "tipo_cambio", precision = 10, scale = 4)
    private BigDecimal tipoCambio;

    // --- TOTALES ---
    @Column(name = "sub_total", precision = 12, scale = 2)
    private BigDecimal subTotal = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal igv = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    // --- SALDOS Y DEUDA ---
    @Column(name = "monto_pagado_inicial", precision = 12, scale = 2)
    private BigDecimal montoPagadoInicial = BigDecimal.ZERO;

    @Column(name = "saldo_pendiente", precision = 12, scale = 2)
    private BigDecimal saldoPendiente = BigDecimal.ZERO;

    // --- IMPUESTOS ESPECÍFICOS ---
    @Column(precision = 12, scale = 2)
    private BigDecimal percepcion = BigDecimal.ZERO;

    @Column(name = "detraccion_porcentaje", precision = 5, scale = 2)
    private BigDecimal detraccionPorcentaje = BigDecimal.ZERO;

    @Column(name = "detraccion_monto", precision = 12, scale = 2)
    private BigDecimal detraccionMonto = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal retencion = BigDecimal.ZERO;

    @Column(length = 500)
    private String observaciones;

    @Enumerated(EnumType.STRING)
    private EstadoCompra estado = EstadoCompra.REGISTRADA;

    // =================================================================
    // ✅ NUEVOS CAMPOS PARA EL MÓDULO DE IMPORTACIONES
    // =================================================================

    // 1. El texto que escribe el usuario (Ej: "2026-01") - Sirve para buscar
    @Column(name = "cod_importacion", length = 50)
    private String codImportacion;

    // 2. La relación real con la carpeta padre (Foreign Key)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "importacion_id")
    @JsonIgnore // Para evitar traer toda la importación al listar compras simples
    private Importacion importacion;

    // 3. Datos necesarios para el cálculo (Prorrateo)
    @Column(name = "peso_neto_kg", precision = 12, scale = 2)
    private BigDecimal pesoNetoKg = BigDecimal.ZERO; // Necesario para distribuir flete

    @Column(name = "bultos")
    private Integer bultos = 0;

    // 4. Resultados del Costeo (Aquí se guardará cuánto le tocó pagar a esta factura)
    @Column(name = "prorrateo_flete", precision = 12, scale = 2)
    private BigDecimal prorrateoFlete = BigDecimal.ZERO;

    @Column(name = "prorrateo_seguro", precision = 12, scale = 2)
    private BigDecimal prorrateoSeguro = BigDecimal.ZERO;

    @Column(name = "prorrateo_gastos_aduanas", precision = 12, scale = 2)
    private BigDecimal prorrateoGastosAduanas = BigDecimal.ZERO; // Agente, Almacén, etc.

    @Column(name = "costo_total_importacion", precision = 12, scale = 2)
    private BigDecimal costoTotalImportacion = BigDecimal.ZERO; // Total Factura + Gastos Prorrateados

    // =================================================================

    // --- RELACIONES ---
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompraDetalle> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PagoCompra> pagos = new ArrayList<>();

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(nullable = false)
    private Boolean activo = true;

    @PrePersist
    void prePersist() {
        this.fechaRegistro = LocalDateTime.now();
        if (this.subTotal == null) this.subTotal = BigDecimal.ZERO;
        if (this.igv == null) this.igv = BigDecimal.ZERO;
        if (this.total == null) this.total = BigDecimal.ZERO;
        if (this.saldoPendiente == null) this.saldoPendiente = BigDecimal.ZERO;
    }

    public void agregarPago(PagoCompra pago) {
        pagos.add(pago);
        pago.setCompra(this);
    }
}