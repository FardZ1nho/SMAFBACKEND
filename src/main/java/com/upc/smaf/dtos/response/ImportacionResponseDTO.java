package com.upc.smaf.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ImportacionResponseDTO {
    private Integer id;
    private String codigoAgrupador;
    private String estado;
    private String tipoTransporte;

    private LocalDate fechaEstimadaLlegada;
    private LocalDate fechaLlegadaReal;

    private String numeroDua;
    private String trackingNumber;
    private String agenteAduanas;
    private String canal;

    private BigDecimal sumaFobTotal;
    private BigDecimal pesoTotalKg;
    private BigDecimal cbmTotal;

    private BigDecimal costoFlete;
    private BigDecimal costoAlmacenajeCft;
    private BigDecimal costoTransporteSjl;
    private BigDecimal costoPersonalDescarga;
    private BigDecimal costoMontacarga;

    private BigDecimal costoDesconsolidacion;

    private BigDecimal costoVistosBuenos;
    private BigDecimal costoTransmision;
    private BigDecimal costoComisionAgencia;
    private BigDecimal costoVobo;
    private BigDecimal costoGastosOperativos;
    private BigDecimal costoResguardo;

    private BigDecimal costoIgv;
    private BigDecimal costoIpm;
    private BigDecimal costoPercepcion;
    private BigDecimal costoAdv;

    private BigDecimal costoOtros1;
    private BigDecimal costoOtros2;
    private BigDecimal costoOtros3;
    private BigDecimal costoOtros4;

    private List<CompraResumenDTO> facturasComerciales;

    @Data
    public static class CompraResumenDTO {
        private Integer id;
        private String serie;
        private String numero;
        private String nombreProveedor;
        private BigDecimal total;
        private String moneda;
        private BigDecimal pesoNetoKg;
        private BigDecimal cbm;

        private BigDecimal proFlete;
        private BigDecimal proAlmacenaje;
        private BigDecimal proTransporte;
        private BigDecimal proPersonalDescarga;
        private BigDecimal proMontacarga;

        private BigDecimal proDesconsolidacion;

        private BigDecimal proVistosBuenos;
        private BigDecimal proTransmision;
        private BigDecimal proComisionAgencia;
        private BigDecimal proVobo;
        private BigDecimal proGastosOperativos;
        private BigDecimal proResguardo;

        private BigDecimal proAdv;
        private BigDecimal proIgv;
        private BigDecimal proIpm;
        private BigDecimal proPercepcion;

        private BigDecimal proOtros1;
        private BigDecimal proOtros2;
        private BigDecimal proOtros3;
        private BigDecimal proOtros4;

        private BigDecimal costoTotalImportacion;

        private List<DetalleItemDTO> items;
    }

    @Data
    public static class DetalleItemDTO {
        // ID DEL DETALLE PARA ENVIARLO AL BACKEND
        private Integer id;

        private String nombreProducto;
        private BigDecimal cantidad;

        // ✅ NUEVO: Agregado para que el servicio pueda mapear y guardar la cantidad recibida
        private Integer cantidadRecibida;

        private BigDecimal precioUnitarioFob;
        private BigDecimal importeFob;

        private BigDecimal factorParticipacion;

        private BigDecimal itemFlete;
        private BigDecimal itemAlmacenaje;
        private BigDecimal itemTransporte;
        private BigDecimal itemDescarga;
        private BigDecimal itemMontacarga;

        private BigDecimal itemDesconsolidacion;

        private BigDecimal itemVistosBuenos;
        private BigDecimal itemTransmision;
        private BigDecimal itemAgente;
        private BigDecimal itemVobo;
        private BigDecimal itemGastosOp;
        private BigDecimal itemResguardo;

        private BigDecimal itemAdv; // Este es el que el Frontend leerá y actualizará
        private BigDecimal itemIgv;
        private BigDecimal itemIpm;
        private BigDecimal itemPercepcion;

        private BigDecimal itemOtros1;
        private BigDecimal itemOtros2;

        private BigDecimal costoUnitarioLanded;
        private BigDecimal costoTotalLanded;
    }
}