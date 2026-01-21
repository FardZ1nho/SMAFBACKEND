package com.upc.smaf.entities;

public enum EstadoImportacion {
    ORDENADO,       // Orden generada
    EN_TRANSITO,    // En barco/avión
    EN_ADUANAS,     // Trámite aduanero
    NACIONALIZADO,  // Impuestos pagados
    EN_ALMACEN,     // Ya ingresó a tu stock
    CERRADO         // Costos finales cerrados
}