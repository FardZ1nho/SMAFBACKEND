package com.upc.smaf.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "compra_detalles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    // El almacén es opcional (nullable = true). Si es servicio, va null.
    @ManyToOne
    @JoinColumn(name = "almacen_id")
    private Almacen almacen;

    @Column(nullable = false)
    private Integer cantidad;

    // Precio FOB Unitario (Costo de compra original en la factura)
    @Column(name = "precio_unitario", precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    // Importe FOB Total (Cantidad * Precio Unitario)
    @Column(name = "importe_total", precision = 12, scale = 2)
    private BigDecimal importeTotal;

    // =================================================================
    // ✅ NUEVOS CAMPOS PARA EL PRORRATEO (COSTOS REALES)
    // =================================================================

    // ✅ NUEVO: Guardar el Ad Valorem ingresado manualmente para este producto
    @Column(name = "ad_valorem_item", precision = 12, scale = 2)
    private BigDecimal adValoremItem = BigDecimal.ZERO;

    // Costo Unitario Final (FOB + Flete + Aduanas + etc. por unidad)
    // Este es el valor que debería ir al Kardex.
    @Column(name = "costo_unitario_landed", precision = 12, scale = 4)
    private BigDecimal costoUnitarioLanded;

    // Costo Total de la línea (Costo Unitario Landed * Cantidad)
    @Column(name = "costo_total_landed", precision = 12, scale = 2)
    private BigDecimal costoTotalLanded;

    // Método helper para calcular el total FOB antes de guardar
    @PrePersist
    @PreUpdate
    public void calcularImporte() {
        if (this.precioUnitario != null && this.cantidad != null) {
            this.importeTotal = this.precioUnitario.multiply(new BigDecimal(this.cantidad));
        }
        // Evitar nulos en el Ad Valorem al guardar
        if (this.adValoremItem == null) {
            this.adValoremItem = BigDecimal.ZERO;
        }
    }
}