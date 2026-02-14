package com.upc.smaf.entities;

public enum TipoProducto {
    PRODUCTO, // Tangible (Controla Stock Físico)
    SERVICIO,
    SUMINISTRO,// Intangible (No controla Stock)
    KIT       // ✅ NUEVO: Compuesto (Stock Virtual calculado por sus componentes)
}