package com.upc.smaf.entities;

public enum MotivoNota {
    ANULACION_DE_LA_OPERACION, // Devuelve todo y stock
    ANULACION_POR_ERROR_EN_RUC,
    CORRECCION_POR_ERROR_EN_LA_DESCRIPCION,
    DESCUENTO_GLOBAL,          // Solo devuelve dinero, no stock
    DEVOLUCION_TOTAL,          // Devuelve todo y stock
    DEVOLUCION_POR_ITEM        // Devuelve parte y stock parcial
}