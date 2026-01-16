package com.upc.smaf.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cuentas_bancarias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CuentaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Nombre interno para identificarla fácil. Ej: "Yape Tienda", "BCP Ahorros"
    @Column(nullable = false, length = 100)
    private String nombre;

    // Institución financiera. Ej: "BCP", "INTERBANK", "BBVA", "SCOTIABANK"
    @Column(nullable = false, length = 50)
    private String banco;

    // Número de cuenta o Celular (para Yape/Plin)
    @Column(nullable = false, length = 50)
    private String numero;

    // ✅ NUEVO CAMPO: CCI (Código de Cuenta Interbancario)
    // Es opcional (nullable = true por defecto) porque Yape no usa CCI
    @Column(name = "cci", length = 30)
    private String cci;

    // Moneda de la cuenta: "PEN" (Soles) o "USD" (Dólares)
    @Column(length = 10, nullable = false)
    private String moneda;

    // Clasificación: "DIGITAL" (Yape/Plin) o "BANCARIA" (Cuentas corrientes/ahorros)
    @Column(length = 20)
    private String tipo;

    // Nombre del titular de la cuenta (Opcional)
    @Column(length = 100)
    private String titular;

    // Para "borrarla" lógicamente sin perder el historial de pagos
    @Column(nullable = false)
    private boolean activa = true;
}