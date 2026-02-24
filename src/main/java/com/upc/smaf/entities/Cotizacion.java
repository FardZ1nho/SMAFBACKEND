package com.upc.smaf.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "cotizaciones")
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String serie;
    private String numero;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    private LocalDateTime fechaEmision;
    private LocalDate fechaVencimiento;

    private String moneda;
    private BigDecimal tipoCambio;

    private BigDecimal subTotal;
    private BigDecimal igv;
    private BigDecimal total;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Enumerated(EnumType.STRING)
    private EstadoCotizacion estado;

    @OneToMany(mappedBy = "cotizacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<CotizacionDetalle> detalles = new ArrayList<>();

    // ⭐⭐⭐ NUEVOS CAMPOS PARA EL CRM ⭐⭐⭐

    @Column(name = "motivo_perdida", length = 255)
    private String motivoPerdida;

    @Column(name = "margen_ganancia_estimado", precision = 10, scale = 2)
    private BigDecimal margenGananciaEstimado;

    // ✅ ACTUALIZADO: Ahora funciona como el Pipeline de Ventas (Embudo)
    public enum EstadoCotizacion {
        CONTACTO_INICIAL,
        COTIZACION_ENVIADA,
        EN_NEGOCIACION,
        GANADA,
        PERDIDA,
        VENCIDA
    }
}