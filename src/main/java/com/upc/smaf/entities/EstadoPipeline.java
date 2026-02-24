package com.upc.smaf.entities;

public enum EstadoPipeline {
    CONTACTO_INICIAL,
    COTIZACION_ENVIADA,
    EN_NEGOCIACION,
    GANADA,    // Al pasar aquí, se crea la Venta y resta stock
    PERDIDA    // Al pasar aquí, pedimos el motivo
}