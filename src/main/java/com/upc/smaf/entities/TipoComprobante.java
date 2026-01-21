package com.upc.smaf.entities;

public enum TipoComprobante {
    FACTURA_ELECTRONICA,
    FACTURA_COMERCIAL, // Solo para Bienes según tu Excel
    BOLETA,
    GUIA_REMISION,     // Solo para Bienes
    NOTA_VENTA,        // Solo para Bienes
    RECIBO_HONORARIOS, // Solo para Servicios
    RECIBO_SIMPLE,     // Solo para Servicios
    OTROS
}