package com.upc.smaf.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "producto_almacen",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_producto", "id_almacen"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoAlmacen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_almacen", nullable = false)
    private Almacen almacen;

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Column(name = "ubicacion_fisica", length = 100)
    private String ubicacionFisica; // Ej: "Pasillo A, Estante 3"

    @Column(name = "stock_minimo")
    private Integer stockMinimo; // Opcional: alerta por almacén específico

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
        if (stock == null) {
            stock = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}