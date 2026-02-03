package com.upc.smaf.dtos.request;

import com.upc.smaf.entities.TipoPago;
import com.upc.smaf.entities.MetodoPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CompraRequestDTO {

    @NotNull(message = "El tipo de compra es obligatorio")
    private String tipoCompra; // BIEN o SERVICIO

    @NotNull(message = "El tipo de comprobante es obligatorio")
    private String tipoComprobante;

    @NotNull(message = "El tipo de pago es obligatorio")
    private TipoPago tipoPago; // CONTADO o CREDITO

    private String serie;
    private String numero;

    @NotNull(message = "La fecha de emisión es obligatoria")
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;

    @NotNull(message = "El proveedor es obligatorio")
    private Integer proveedorId;

    private String moneda;
    private BigDecimal tipoCambio;
    private String observaciones;

    // Totales
    private BigDecimal subTotal;
    private BigDecimal igv;
    private BigDecimal total;

    // Impuestos Específicos
    private BigDecimal percepcion;
    private BigDecimal detraccionPorcentaje;
    private BigDecimal detraccionMonto;
    private BigDecimal retencion;

    // =================================================================
    // ✅ CAMPOS CLAVE PARA IMPORTACIÓN
    // =================================================================

    // Si el usuario pone "2026-01", el backend busca esa Importación y la vincula.
    private String codImportacion;

    // Vital para prorratear el flete por peso
    private BigDecimal pesoNetoKg;

    private Integer bultos;

    // =================================================================

    @Valid
    private List<PagoCompraRequestDTO> pagos;

    @Valid
    private List<DetalleRequestDTO> detalles;

    // --- CLASES INTERNAS ---
    @Data
    public static class PagoCompraRequestDTO {
        @NotNull
        private MetodoPago metodoPago;
        @NotNull @Positive
        private BigDecimal monto;
        @NotNull
        private String moneda;
        private Integer cuentaOrigenId;
        private String referencia;
    }

    @Data
    public static class DetalleRequestDTO {
        private Integer productoId;
        private Integer almacenId;
        private Integer cantidad;
        private BigDecimal precioUnitario;
    }
}