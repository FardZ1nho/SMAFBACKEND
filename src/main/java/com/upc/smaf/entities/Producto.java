package com.upc.smaf.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "codigo", unique = true, length = 50)
    private String codigo;

    @Column(name = "codigo_internacional", length = 50)
    private String codigoInternacional;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20, nullable = false)
    private TipoProducto tipo = TipoProducto.PRODUCTO; // Asegúrate de que el Enum tenga 'KIT'

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Categoria categoria;

    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual = 0;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 5;

    // ✅ RELACIÓN 1: Stock Físico en Almacenes (Para Productos Simples)
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductoAlmacen> productosAlmacen = new ArrayList<>();

    // ✅ RELACIÓN 2: Componentes del Kit (Solo se usa si tipo == KIT)
    @OneToMany(mappedBy = "kit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductoKit> componentes = new ArrayList<>();

    @Column(name = "precio_china", precision = 10, scale = 2)
    private BigDecimal precioChina;

    @Column(name = "costo_total", precision = 10, scale = 2)
    private BigDecimal costoTotal;

    @Column(name = "precio_venta", precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "moneda", length = 3)
    private String moneda = "USD";

    @Column(name = "unidad_medida", length = 20)
    private String unidadMedida = "unidad";

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    // ✅ MÉTODO HELPER PARA AGREGAR COMPONENTES FÁCILMENTE
    public void agregarComponente(Producto componente, int cantidad) {
        ProductoKit pk = new ProductoKit();
        pk.setKit(this);
        pk.setComponente(componente);
        pk.setCantidad(cantidad);
        this.componentes.add(pk);
    }

    public void calcularStockTotal() {
        // Si es un KIT, el stock físico (productosAlmacen) suele ser 0,
        // porque el stock es "virtual" (depende de los hijos).
        // Mantenemos esta lógica para productos simples.
        this.stockActual = productosAlmacen.stream()
                .filter(pa -> pa.getActivo())
                .mapToInt(ProductoAlmacen::getStock)
                .sum();
    }
}