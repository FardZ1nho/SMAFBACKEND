package com.upc.smaf.entities;

import com.upc.smaf.entities.TipoCompra;
import com.upc.smaf.entities.TipoComprobante;
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
@Table(name = "compras")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ✅ NUEVO: Define si es BIEN o SERVICIO (Controla qué inputs se ven)
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_compra", nullable = false, length = 20)
    private TipoCompra tipoCompra;

    // ✅ Actualizado a Enum para evitar errores de tipeo
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprobante", nullable = false, length = 30)
    private TipoComprobante tipoComprobante;

    @Column(nullable = false, length = 10)
    private String serie;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(name = "fec_emision", nullable = false)
    private LocalDate fechaEmision; // Cambié nombre variable a estándar camelCase

    @Column(name = "fec_vencimiento")
    private LocalDate fechaVencimiento; // Útil para cuentas por pagar

    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(length = 10)
    private String moneda = "PEN"; // PEN o USD

    @Column(name = "tipo_cambio", precision = 10, scale = 4)
    private BigDecimal tipoCambio; // Usar BigDecimal para precisión financiera

    // ==========================================
    // 💰 MONTOS Y TOTALES (Nuevos)
    // ==========================================

    @Column(name = "sub_total", precision = 12, scale = 2)
    private BigDecimal subTotal = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal igv = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO; // Total a pagar al proveedor

    // ==========================================
    // 📊 IMPUESTOS ESPECÍFICOS (Según tu Excel)
    // ==========================================

    // Solo para BIENES (Factura Comercial / Electrónica)
    @Column(precision = 12, scale = 2)
    private BigDecimal percepcion = BigDecimal.ZERO;

    // Solo para SERVICIOS (Detracciones)
    @Column(name = "detraccion_porcentaje", precision = 5, scale = 2)
    private BigDecimal detraccionPorcentaje = BigDecimal.ZERO; // Ej: 10.00%

    @Column(name = "detraccion_monto", precision = 12, scale = 2)
    private BigDecimal detraccionMonto = BigDecimal.ZERO;      // Ej: 50.00 soles

    // Común (Retenciones)
    @Column(precision = 12, scale = 2)
    private BigDecimal retencion = BigDecimal.ZERO;

    @Column(length = 500)
    private String observaciones;

    // ==========================================
    // RELACIONES Y AUDITORÍA
    // ==========================================

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompraDetalle> detalles = new ArrayList<>();

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(nullable = false)
    private Boolean activo = true; // Para borrado lógico

    @PrePersist
    void prePersist() {
        this.fechaRegistro = LocalDateTime.now();
        if (this.subTotal == null) this.subTotal = BigDecimal.ZERO;
        if (this.igv == null) this.igv = BigDecimal.ZERO;
        if (this.total == null) this.total = BigDecimal.ZERO;
        if (this.moneda == null) this.moneda = "PEN";
    }
}