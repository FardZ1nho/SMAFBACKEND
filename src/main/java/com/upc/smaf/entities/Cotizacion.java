package com.upc.smaf.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data; // 👈 Esto genera los getters automáticos
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

    // ✅ ESTOS CAMPOS FALTABAN, POR ESO LOS ERRORES EN ROJO
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

    public enum EstadoCotizacion {
        BORRADOR, ENVIADA, APROBADA, RECHAZADA, VENCIDA
    }
}