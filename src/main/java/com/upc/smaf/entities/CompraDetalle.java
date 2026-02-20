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

    @ManyToOne
    @JoinColumn(name = "almacen_id")
    private Almacen almacen;

    @Column(nullable = false)
    private Integer cantidad;

    // ✅ NUEVO CAMPO: Para validar contra lo facturado
    @Column(name = "cantidad_recibida")
    private Integer cantidadRecibida = 0;

    @Column(name = "precio_unitario", precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "importe_total", precision = 12, scale = 2)
    private BigDecimal importeTotal;

    @Column(name = "ad_valorem_item", precision = 12, scale = 2)
    private BigDecimal adValoremItem = BigDecimal.ZERO;

    @Column(name = "costo_unitario_landed", precision = 12, scale = 4)
    private BigDecimal costoUnitarioLanded;

    @Column(name = "costo_total_landed", precision = 12, scale = 2)
    private BigDecimal costoTotalLanded;

    @PrePersist
    @PreUpdate
    public void calcularImporte() {
        if (this.precioUnitario != null && this.cantidad != null) {
            this.importeTotal = this.precioUnitario.multiply(new BigDecimal(this.cantidad));
        }
        if (this.adValoremItem == null) {
            this.adValoremItem = BigDecimal.ZERO;
        }
        if (this.cantidadRecibida == null) {
            this.cantidadRecibida = 0;
        }
    }
}