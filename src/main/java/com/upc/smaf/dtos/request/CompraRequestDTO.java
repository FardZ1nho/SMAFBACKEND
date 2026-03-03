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
    private String tipoCompra;

    @NotNull(message = "El tipo de comprobante es obligatorio")
    private String tipoComprobante;

    @NotNull(message = "El tipo de pago es obligatorio")
    private TipoPago tipoPago;

    private String serie;
    private String numero;

    @NotNull(message = "La fecha de emisión es obligatoria")
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;

    // ✅ CORRECCIÓN: Se quitó @NotNull porque ahora el ID puede ser nulo (Proveedor Libre)
    private Integer proveedorId;

    // ✅ NUEVO: Para recibir el nombre del proveedor libre desde Angular
    private String nombreProveedor;

    private String moneda;
    private BigDecimal tipoCambio;
    private String observaciones;

    // --- TOTALES ---
    private BigDecimal subTotal;
    private BigDecimal fob; // ✅ FOB Adicional
    private BigDecimal igv;
    private BigDecimal total;

    // Impuestos Específicos (Locales)
    private BigDecimal percepcion;
    private BigDecimal detraccionPorcentaje;
    private BigDecimal detraccionMonto;
    private BigDecimal retencion;

    // =================================================================
    // CAMPOS IMPORTACIÓN
    // =================================================================
    private String codImportacion;

    private BigDecimal pesoNetoKg;

    // ✅ CAMBIO: De 'bultos' (Integer) a 'cbm' (BigDecimal)
    private BigDecimal cbm;

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
        // Puede ser null si es un ítem de texto libre
        private Integer productoId;

        // ✅ NUEVO: Para recibir el nombre del ítem digitado manualmente
        private String nombreProducto;

        private Integer almacenId;
        private Integer cantidad;
        private BigDecimal precioUnitario;
    }
}