package com.upc.smaf.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitario;

    @Column(precision = 5, scale = 2)
    private BigDecimal descuento; // Porcentaje de descuento

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;

    // Calcular subtotal automáticamente
    public void calcularSubtotal() {
        BigDecimal total = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        if (descuento != null && descuento.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal descuentoMonto = total.multiply(descuento).divide(BigDecimal.valueOf(100));
            total = total.subtract(descuentoMonto);
        }
        this.subtotal = total;
    }
}