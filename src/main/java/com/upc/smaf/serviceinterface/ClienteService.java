package com.upc.smaf.serviceinterface;

import com.upc.smaf.dtos.request.ClienteRequestDTO;
import com.upc.smaf.dtos.response.ClienteResponseDTO;
import java.util.List;

public interface ClienteService {

    // CRUD básico
    ClienteResponseDTO crearCliente(ClienteRequestDTO request);
    ClienteResponseDTO obtenerCliente(Integer id);
    List<ClienteResponseDTO> listarClientes();
    List<ClienteResponseDTO> listarClientesActivos();
    ClienteResponseDTO actualizarCliente(Integer id, ClienteRequestDTO request);
    void desactivarCliente(Integer id);

    // Búsquedas
    ClienteResponseDTO obtenerClientePorDocumento(String numeroDocumento);
    List<ClienteResponseDTO> buscarClientesPorNombre(String nombre);
    List<ClienteResponseDTO> buscarClientes(String termino);

    // Filtros
    List<ClienteResponseDTO> listarClientesPorTipo(String tipoCliente);
}