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

    // ==========================================
    // 💰 DATOS DE LA FACTURA (FOB)
    // ==========================================

    @Column(name = "sub_total", precision = 12, scale = 2)
    private BigDecimal subTotal = BigDecimal.ZERO;

    // FOB: Valor de la mercancía (Base para prorrateo por Valor)
    @Column(name = "fob", precision = 12, scale = 2)
    private BigDecimal fob = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal igv = BigDecimal.ZERO; // Se mantiene en 0 por defecto

    @Column(precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO; // Total = SubTotal + FOB

    // =================================================================
    // 📦 DATOS LOGÍSTICOS (BASES PARA PRORRATEO)
    // =================================================================

    @Column(name = "peso_neto_kg", precision = 12, scale = 2)
    private BigDecimal pesoNetoKg = BigDecimal.ZERO; // Base para prorrateo por Peso

    // ✅ REEMPLAZO DE BULTOS: Ahora es CBM (Volumen)
    @Column(name = "cbm", precision = 12, scale = 2)
    private BigDecimal cbm = BigDecimal.ZERO; // Base para prorrateo por Volumen

    // =================================================================
    // 🧮 RESULTADOS DEL PRORRATEO (Cuánto paga ESTA factura)
    // =================================================================

    // Resultados Grupo Volumen (CBM)
    @Column(precision = 12, scale = 2) private BigDecimal proFlete = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal proAlmacenaje = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal proTransporte = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal proCargaDescarga = BigDecimal.ZERO; // Personal + Montacarga

    // Resultados Grupo Peso (KG)
    @Column(precision = 12, scale = 2) private BigDecimal proDesconsolidacion = BigDecimal.ZERO;

    // Resultados Grupo Valor (FOB)
    @Column(precision = 12, scale = 2) private BigDecimal proGastosAduaneros = BigDecimal.ZERO; // Vistos, Transmision, Vobo, Comision
    @Column(precision = 12, scale = 2) private BigDecimal proSeguroResguardo = BigDecimal.ZERO; // Resguardo
    @Column(precision = 12, scale = 2) private BigDecimal proImpuestos = BigDecimal.ZERO; // IGV + IPM + Percep
    @Column(precision = 12, scale = 2) private BigDecimal proOtrosGastos = BigDecimal.ZERO; // Otros 1-4 + ADV

    // ✅ COSTO FINAL (Landed Cost)
    @Column(name = "costo_total_importacion", precision = 12, scale = 2)
    private BigDecimal costoTotalImportacion = BigDecimal.ZERO;

    // =================================================================

    // Relación con Importación
    @Column(name = "cod_importacion", length = 50)
    private String codImportacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "importacion_id")
    @JsonIgnore
    private Importacion importacion;

    // --- SALDOS Y DEUDA ---
    @Column(name = "monto_pagado_inicial", precision = 12, scale = 2)
    private BigDecimal montoPagadoInicial = BigDecimal.ZERO;

    @Column(name = "saldo_pendiente", precision = 12, scale = 2)
    private BigDecimal saldoPendiente = BigDecimal.ZERO;

    // --- IMPUESTOS LOCALES (Si aplica) ---
    @Column(precision = 12, scale = 2) private BigDecimal percepcion = BigDecimal.ZERO;
    @Column(name = "detraccion_porcentaje", precision = 5, scale = 2) private BigDecimal detraccionPorcentaje = BigDecimal.ZERO;
    @Column(name = "detraccion_monto", precision = 12, scale = 2) private BigDecimal detraccionMonto = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2) private BigDecimal retencion = BigDecimal.ZERO;

    @Column(length = 500)
    private String observaciones;

    @Enumerated(EnumType.STRING)
    private EstadoCompra estado = EstadoCompra.REGISTRADA;

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
        if (this.fob == null) this.fob = BigDecimal.ZERO;
        if (this.igv == null) this.igv = BigDecimal.ZERO;
        if (this.total == null) this.total = BigDecimal.ZERO;
        if (this.saldoPendiente == null) this.saldoPendiente = BigDecimal.ZERO;

        // Inicializar bases
        if (this.cbm == null) this.cbm = BigDecimal.ZERO;
        if (this.pesoNetoKg == null) this.pesoNetoKg = BigDecimal.ZERO;
    }

    public void agregarPago(PagoCompra pago) {
        pagos.add(pago);
        pago.setCompra(this);
    }

    public void agregarDetalle(CompraDetalle detalle) {
        detalles.add(detalle);
        detalle.setCompra(this);
    }
}