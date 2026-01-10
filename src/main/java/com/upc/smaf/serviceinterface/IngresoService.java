package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.request.IngresoRequestDTO;
import com.upc.smaf.dtos.response.IngresoResponseDTO;
import java.util.List;

public interface IngresoService {

    /**
     * Registra una nueva entrada de mercadería y aumenta el stock del producto
     * @param request Datos del ingreso (idProducto, cantidad, etc.)
     * @return Detalle del ingreso registrado
     */
    IngresoResponseDTO registrarIngreso(IngresoRequestDTO request);

    /**
     * Lista todos los ingresos realizados (historial)
     * @return Lista de historial de entradas
     */
    List<IngresoResponseDTO> listarHistorialIngresos();

    /**
     * Obtener ingresos de un producto específico
     * @param idProducto ID del producto a consultar
     * @return Lista de ingresos filtrada
     */
    List<IngresoResponseDTO> listarIngresosPorProducto(Integer idProducto);
}