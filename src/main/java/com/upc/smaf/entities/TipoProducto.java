package com.upc.smaf.entities;

public enum TipoProducto {
    PRODUCTO, // Tangible (Martillo, Cemento) -> Controla Stock
    SERVICIO  // Intangible (Mano de obra, Flete) -> No controla Stock
}