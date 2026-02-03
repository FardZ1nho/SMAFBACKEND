package com.upc.smaf.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "cotizacion_detalles")
public class CotizacionDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_cotizacion")
    @JsonBackReference
    private Cotizacion cotizacion;

    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Producto producto;

    private Integer cantidad;
    private BigDecimal precioUnitario; // Precio al momento de cotizar
    private BigDecimal importe; // Cantidad * Precio
}