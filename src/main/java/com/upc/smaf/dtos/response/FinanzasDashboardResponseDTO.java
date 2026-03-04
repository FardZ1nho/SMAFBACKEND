package com.upc.smaf.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FinanzasDashboardResponseDTO {

    // --- KPIs (TARJETAS SUPERIORES) ---
    private BigDecimal totalIngresosEfectivos; // Ventas cobradas + Ingresos Caja
    private BigDecimal totalEgresosEfectivos;  // Compras pagadas + Egresos Caja
    private BigDecimal balanceNeto;            // Ingresos - Egresos

    // --- IMPUESTOS (ESCUDO FISCAL) ---
    private BigDecimal totalIgvPercibido;  // IGV de Ventas (Debes a SUNAT)
    private BigDecimal totalIgvPagado;     // IGV de Compras (A favor tuyo)
    private BigDecimal balanceIgv;         // Percibido - Pagado

    private BigDecimal totalRetenciones;
    private BigDecimal totalDetracciones;
    private BigDecimal totalPercepciones;

    // --- LA TABLA UNIFICADA ---
    private List<TransaccionDTO> transacciones;

    @Data
    public static class TransaccionDTO {
        private LocalDateTime fechaHora;
        private String tipo;          // "INGRESO" o "EGRESO"
        private String origen;        // "VENTA", "COMPRA", "CAJA_CHICA"
        private String tipoComprobante; // "FACTURA", "BOLETA", "RECIBO", etc.
        private String comprobante;   // Ej: "F001-002"
        private String entidad;       // Nombre del Cliente o Proveedor
        private String moneda;
        private BigDecimal montoTotal;
    }
}