package com.upc.smaf.entities;

public enum TipoProducto {
    PRODUCTO, // Tangible (Controla Stock Físico)
    SERVICIO, // Intangible (No controla Stock)
    KIT       // ✅ NUEVO: Compuesto (Stock Virtual calculado por sus componentes)
}