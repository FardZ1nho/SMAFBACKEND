package com.upc.smaf.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "compras")
@Data
public class Compra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tipo_comprobante", nullable = false)
    private String tipoComprobante; // Factura, Boleta, etc.

    @Column(nullable = false, length = 10)
    private String serie;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(name = "fec_emision", nullable = false)
    private LocalDate fecEmision;

    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(length = 10)
    private String moneda; // Soles, Dólares

    @Column(name = "tipo_cambio")
    private Double tipoCambio;

    @Column(length = 500)
    private String observaciones;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    private List<CompraDetalle> detalles;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @PrePersist
    void prePersist() {
        this.fechaRegistro = LocalDateTime.now();
    }
}