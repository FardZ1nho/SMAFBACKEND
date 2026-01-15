package com.upc.smaf.entities;

import com.upc.smaf.entities.Almacen;
import com.upc.smaf.entities.Compra;
import com.upc.smaf.entities.Producto;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "compra_detalles")
@Data
public class CompraDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "compra_id")
    private Compra compra;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "almacen_id")
    private Almacen almacen;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario")
    private Double precioUnitario; // Importante para costos
}