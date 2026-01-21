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

    // ✅ CAMBIO 1: El almacén es opcional (nullable = true por defecto)
    // Si es BIEN -> Se llena. Si es SERVICIO -> Se deja en null.
    @ManyToOne
    @JoinColumn(name = "almacen_id")
    private Almacen almacen;

    @Column(nullable = false)
    private Integer cantidad;

    // ✅ CAMBIO 2: Usar BigDecimal para no perder centavos
    @Column(name = "precio_unitario", precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    // ✅ CAMBIO 3: Campo calculado útil (Cantidad * Precio)
    @Column(name = "importe_total", precision = 12, scale = 2)
    private BigDecimal importeTotal;

    // Método helper para calcular el total de la línea antes de guardar
    public void calcularImporte() {
        if (this.precioUnitario != null && this.cantidad != null) {
            this.importeTotal = this.precioUnitario.multiply(new BigDecimal(this.cantidad));
        }
    }
}