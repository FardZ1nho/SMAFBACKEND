package com.upc.smaf.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FinanzasDashboardResponseDTO {
    private BigDecimal totalIngresosEfectivos;
    private BigDecimal totalEgresosEfectivos;
    private BigDecimal balanceNeto;

    private BigDecimal totalIgvPercibido;
    private BigDecimal totalIgvPagado;
    private BigDecimal balanceIgv;

    private BigDecimal totalRetenciones;
    private BigDecimal totalDetracciones;
    private BigDecimal totalPercepciones;

    private List<TransaccionDTO> transacciones;

    // ✅ ¡AQUÍ ESTÁ LA CLAVE! La etiqueta @Data debe estar aquí
    @Data
    public static class TransaccionDTO {
        private LocalDateTime fechaHora;
        private String tipo;
        private String origen;
        private String tipoComprobante;
        private String comprobante;
        private String entidad;
        private String moneda;
        private BigDecimal montoTotal;

        // Nuevos campos para el reporte
        private String ruc;
        private String descripcion;
        private BigDecimal subTotal;
        private BigDecimal igv;
        private BigDecimal tipoCambio;
    }
}