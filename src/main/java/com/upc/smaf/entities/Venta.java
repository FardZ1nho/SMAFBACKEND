package com.upc.smaf.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "fecha_venta", nullable = false)
    private LocalDateTime fechaVenta;

    @Column(name = "nombre_cliente", length = 100)
    private String nombreCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cliente", nullable = false)
    private TipoCliente tipoCliente;

    // Define si es CONTADO o CREDITO
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago", nullable = false)
    private TipoPago tipoPago;

    // Método de pago principal (o de la inicial si es crédito)
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    private MetodoPago metodoPago;

    // --- CAMPOS DE PAGO MIXTO ---
    @Column(name = "pago_efectivo", precision = 10, scale = 2)
    private BigDecimal pagoEfectivo;

    @Column(name = "pago_transferencia", precision = 10, scale = 2)
    private BigDecimal pagoTransferencia;

    // --- CAMPOS DE CRÉDITO ---
    @Column(name = "monto_inicial", precision = 10, scale = 2)
    private BigDecimal montoInicial;

    @Column(name = "numero_cuotas")
    private Integer numeroCuotas;

    @Column(name = "monto_cuota", precision = 10, scale = 2)
    private BigDecimal montoCuota;

    @Column(name = "saldo_pendiente", precision = 10, scale = 2)
    private BigDecimal saldoPendiente;

    // --- CAMPOS DE MONEDA Y DOCUMENTO ---
    @Column(length = 3)
    private String moneda;

    @Column(name = "tipo_cambio", precision = 10, scale = 4)
    private BigDecimal tipoCambio;

    @Column(name = "tipo_documento", length = 20)
    private String tipoDocumento;

    @Column(name = "numero_documento", length = 50)
    private String numeroDocumento;

    // --- TOTALES ---
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal igv;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal total;

    @Column(length = 500)
    private String notas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVenta estado;

    // --- RELACIONES ---

    // Detalles de productos vendidos
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();

    // Historial de Pagos / Amortizaciones
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pago> pagos = new ArrayList<>();

    // ✅ NUEVO: Cuenta donde entró el dinero HOY (Total o Inicial)
    @ManyToOne
    @JoinColumn(name = "cuenta_bancaria_id", nullable = true)
    private CuentaBancaria cuentaBancaria;

    // --- AUDITORÍA ---
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // --- MÉTODOS DE CICLO DE VIDA ---
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();

        if (montoInicial == null) montoInicial = BigDecimal.ZERO;
        if (saldoPendiente == null) saldoPendiente = BigDecimal.ZERO;
        if (numeroCuotas == null) numeroCuotas = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // --- MÉTODOS AUXILIARES ---
    public void agregarDetalle(DetalleVenta detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
    }

    public void agregarPago(Pago pago) {
        pagos.add(pago);
        pago.setVenta(this);
    }
}