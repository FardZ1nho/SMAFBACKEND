package com.upc.smaf.entities;

public enum EstadoCompra {
    REGISTRADA,   // Se creó pero aun se debe dinero
    COMPLETADA,   // Se pagó totalmente (o era al contado)
    ANULADA       // Se canceló la compra
}