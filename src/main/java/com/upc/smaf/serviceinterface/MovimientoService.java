package com.upc.smaf.serviceinterface;

import com.upc.smaf.entities.Movimiento;
import com.upc.smaf.entities.Movimiento.TipoMovimiento;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoService {

    /**
     * Registrar traslado entre almacenes
     */
    Movimiento registrarTraslado(Integer productoId, Long almacenOrigenId, Long almacenDestinoId,
                                 Integer cantidad, String motivo);

    /**
     * Registrar entrada de mercancía
     */
    Movimiento registrarEntrada(Integer productoId, Long almacenDestinoId,
                                Integer cantidad, String motivo);

    /**
     * Registrar salida de mercancía
     */
    Movimiento registrarSalida(Integer productoId, Long almacenOrigenId,
                               Integer cantidad, String motivo);

    /**
     * Registrar ajuste de inventario
     */
    Movimiento registrarAjuste(Integer productoId, Long almacenId,
                               Integer cantidad, String motivo);

    /**
     * Listar todos los movimientos
     */
    List<Movimiento> listarTodos();

    /**
     * Listar movimientos por tipo
     */
    List<Movimiento> listarPorTipo(TipoMovimiento tipo);

    /**
     * Listar movimientos por producto
     */
    List<Movimiento> listarPorProducto(Integer productoId);

    /**
     * Listar movimientos por almacén
     */
    List<Movimiento> listarPorAlmacen(Long almacenId);

    /**
     * Listar movimientos entre fechas
     */
    List<Movimiento> listarPorFechas(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtener movimiento por ID
     */
    Movimiento obtenerPorId(Long id);
}