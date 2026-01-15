package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.request.ProveedorRequestDTO;
import com.upc.smaf.dtos.response.ProveedorResponseDTO;

import java.util.List;

public interface ProveedorService {
    ProveedorResponseDTO crearProveedor(ProveedorRequestDTO request);
    ProveedorResponseDTO obtenerProveedor(Integer id);
    List<ProveedorResponseDTO> listarProveedores();
    List<ProveedorResponseDTO> listarProveedoresActivos();
    ProveedorResponseDTO actualizarProveedor(Integer id, ProveedorRequestDTO request);
    void desactivarProveedor(Integer id);
    List<ProveedorResponseDTO> buscarPorNombre(String nombre);
    ProveedorResponseDTO obtenerPorRuc(String ruc);
}