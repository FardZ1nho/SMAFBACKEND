package com.upc.smaf.serviceinterface;

import com.upc.smaf.entities.CuentaBancaria;
import java.util.List;

public interface CuentaBancariaService {
    List<CuentaBancaria> listarTodas();
    List<CuentaBancaria> listarActivas(); // Para los desplegables de venta
    CuentaBancaria obtenerPorId(Integer id);
    CuentaBancaria guardar(CuentaBancaria cuenta);
    CuentaBancaria actualizar(Integer id, CuentaBancaria cuenta);
    void eliminar(Integer id); // Borrado físico (solo si no tiene movimientos)
    void desactivar(Integer id); // Borrado lógico (recomendado)
}