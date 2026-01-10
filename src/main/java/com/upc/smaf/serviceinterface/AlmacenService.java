package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.request.AlmacenRequestDTO;
import com.upc.smaf.dtos.response.AlmacenResponseDTO;

import java.util.List;

public interface AlmacenService {

    // Crear un nuevo almacén
    AlmacenResponseDTO crearAlmacen(AlmacenRequestDTO request);

    // Actualizar un almacén existente
    AlmacenResponseDTO actualizarAlmacen(Long id, AlmacenRequestDTO request);

    // Obtener un almacén por ID
    AlmacenResponseDTO obtenerAlmacenPorId(Long id);

    // Obtener un almacén por código
    AlmacenResponseDTO obtenerAlmacenPorCodigo(String codigo);

    // Listar todos los almacenes
    List<AlmacenResponseDTO> listarTodosLosAlmacenes();

    // Listar solo almacenes activos
    List<AlmacenResponseDTO> listarAlmacenesActivos();

    // Activar/Desactivar almacén (borrado lógico)
    void cambiarEstadoAlmacen(Long id, Boolean activo);

    // Eliminar almacén (borrado físico - solo si no tiene productos)
    void eliminarAlmacen(Long id);
}