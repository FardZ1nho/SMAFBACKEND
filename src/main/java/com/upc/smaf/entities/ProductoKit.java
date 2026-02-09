package com.upc.smaf.entities;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "producto_kits")
@Data
public class ProductoKit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // EL PADRE (El Kit, ej: "Bull Float Completo")
    @ManyToOne
    @JoinColumn(name = "kit_id")
    @JsonIgnore // Evita bucles infinitos al convertir a JSON
    private Producto kit;

    // EL HIJO (El componente, ej: "Tubo de Aluminio")
    @ManyToOne
    @JoinColumn(name = "componente_id")
    private Producto componente;

    // CANTIDAD (Ej: 3 unidades)
    private Integer cantidad;
}