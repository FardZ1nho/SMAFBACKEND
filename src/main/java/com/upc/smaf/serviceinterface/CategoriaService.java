package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.request.CategoriaRequestDTO;
import com.upc.smaf.dtos.response.CategoriaResponseDTO;
import java.util.List;

public interface CategoriaService {

    CategoriaResponseDTO crear(CategoriaRequestDTO categoriaRequestDTO);

    CategoriaResponseDTO actualizar(Integer id, CategoriaRequestDTO categoriaRequestDTO);

    CategoriaResponseDTO obtenerPorId(Integer id);

    List<CategoriaResponseDTO> listarTodas();

    List<CategoriaResponseDTO> listarActivas();

    List<CategoriaResponseDTO> buscarPorNombre(String nombre);

    void eliminar(Integer id);

    void activarDesactivar(Integer id, Boolean activo);
}