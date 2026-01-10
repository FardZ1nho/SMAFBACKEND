package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.ClienteRequestDTO;
import com.upc.smaf.dtos.response.ClienteResponseDTO;
import com.upc.smaf.entities.Cliente;
import com.upc.smaf.repositories.ClienteRepository;
import com.upc.smaf.serviceinterface.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    @Transactional
    public ClienteResponseDTO crearCliente(ClienteRequestDTO request) {
        // Validar según tipo de cliente (PERSONA o EMPRESA)
        validarTipoCliente(request);

        // Validar que el documento no exista
        if (request.getNumeroDocumento() != null &&
                clienteRepository.existsByNumeroDocumento(request.getNumeroDocumento())) {
            throw new RuntimeException("Ya existe un cliente con ese número de documento");
        }

        Cliente cliente = new Cliente();
        mapearRequestAEntidad(request, cliente);
        cliente.setActivo(true);

        cliente = clienteRepository.save(cliente);
        return convertirAResponseDTO(cliente);
    }

    @Override
    public ClienteResponseDTO obtenerCliente(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        return convertirAResponseDTO(cliente);
    }

    @Override
    public List<ClienteResponseDTO> listarClientes() {
        return clienteRepository.findAll().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClienteResponseDTO> listarClientesActivos() {
        return clienteRepository.findByActivoTrue().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClienteResponseDTO actualizarCliente(Integer id, ClienteRequestDTO request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Validar según tipo de cliente
        validarTipoCliente(request);

        // Validar documento único (si cambió)
        if (request.getNumeroDocumento() != null &&
                !request.getNumeroDocumento().equals(cliente.getNumeroDocumento()) &&
                clienteRepository.existsByNumeroDocumento(request.getNumeroDocumento())) {
            throw new RuntimeException("Ya existe un cliente con ese número de documento");
        }

        mapearRequestAEntidad(request, cliente);
        cliente = clienteRepository.save(cliente);
        return convertirAResponseDTO(cliente);
    }

    @Override
    @Transactional
    public void desactivarCliente(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    @Override
    public ClienteResponseDTO obtenerClientePorDocumento(String numeroDocumento) {
        Cliente cliente = clienteRepository.findByNumeroDocumentoAndActivoTrue(numeroDocumento)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        return convertirAResponseDTO(cliente);
    }

    @Override
    public List<ClienteResponseDTO> buscarClientesPorNombre(String nombre) {
        return clienteRepository.findByNombreCompletoContainingIgnoreCaseAndActivoTrue(nombre).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClienteResponseDTO> buscarClientes(String termino) {
        return clienteRepository.buscarClientes(termino).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClienteResponseDTO> listarClientesPorTipo(String tipoCliente) {
        return clienteRepository.findByTipoClienteAndActivoTrue(tipoCliente).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    // ========== MÉTODOS PRIVADOS DE VALIDACIÓN ==========

    /**
     * Validar cliente según su tipo (PERSONA o EMPRESA)
     */
    private void validarTipoCliente(ClienteRequestDTO request) {
        String tipo = request.getTipoCliente();

        if (tipo == null || tipo.trim().isEmpty()) {
            throw new RuntimeException("El tipo de cliente es obligatorio");
        }

        if ("PERSONA".equalsIgnoreCase(tipo)) {
            validarPersona(request);
        } else if ("EMPRESA".equalsIgnoreCase(tipo)) {
            validarEmpresa(request);
        } else {
            throw new RuntimeException("Tipo de cliente inválido. Debe ser PERSONA o EMPRESA");
        }
    }

    /**
     * Validar datos de PERSONA (DNI)
     */
    private void validarPersona(ClienteRequestDTO request) {
        // Nombre completo obligatorio
        if (request.getNombreCompleto() == null || request.getNombreCompleto().trim().isEmpty()) {
            throw new RuntimeException("El nombre completo es obligatorio para personas");
        }

        // Validar DNI si se proporciona
        if ("DNI".equalsIgnoreCase(request.getTipoDocumento())) {
            if (request.getNumeroDocumento() == null || request.getNumeroDocumento().trim().isEmpty()) {
                throw new RuntimeException("El número de DNI es obligatorio");
            }

            // DNI debe tener exactamente 8 dígitos
            if (!request.getNumeroDocumento().matches("\\d{8}")) {
                throw new RuntimeException("El DNI debe tener exactamente 8 dígitos");
            }
        }

        // Validar PASAPORTE si se proporciona
        if ("PASAPORTE".equalsIgnoreCase(request.getTipoDocumento())) {
            if (request.getNumeroDocumento() == null || request.getNumeroDocumento().trim().isEmpty()) {
                throw new RuntimeException("El número de pasaporte es obligatorio");
            }
        }

        // Validar CARNET DE EXTRANJERÍA si se proporciona
        if ("CARNET_EXTRANJERIA".equalsIgnoreCase(request.getTipoDocumento())) {
            if (request.getNumeroDocumento() == null || request.getNumeroDocumento().trim().isEmpty()) {
                throw new RuntimeException("El número de carnet de extranjería es obligatorio");
            }
        }
    }

    /**
     * Validar datos de EMPRESA (RUC)
     */
    private void validarEmpresa(ClienteRequestDTO request) {
        // Razón social obligatoria
        if (request.getRazonSocial() == null || request.getRazonSocial().trim().isEmpty()) {
            throw new RuntimeException("La razón social es obligatoria para empresas");
        }

        // Validar RUC
        if ("RUC".equalsIgnoreCase(request.getTipoDocumento())) {
            if (request.getNumeroDocumento() == null || request.getNumeroDocumento().trim().isEmpty()) {
                throw new RuntimeException("El número de RUC es obligatorio");
            }

            // RUC debe tener exactamente 11 dígitos
            if (!request.getNumeroDocumento().matches("\\d{11}")) {
                throw new RuntimeException("El RUC debe tener exactamente 11 dígitos");
            }

            // RUC debe empezar con 10 (persona jurídica) o 20 (empresa)
            String ruc = request.getNumeroDocumento();
            if (!ruc.startsWith("10") && !ruc.startsWith("20")) {
                throw new RuntimeException("El RUC debe comenzar con 10 o 20");
            }
        } else {
            throw new RuntimeException("Las empresas deben usar RUC como tipo de documento");
        }

        // Nombre de contacto recomendado (no obligatorio, solo warning)
        if (request.getNombreContacto() == null || request.getNombreContacto().trim().isEmpty()) {
            System.out.println("⚠️ Recomendación: Agregar nombre de contacto para la empresa");
        }
    }

    // ========== MÉTODOS PRIVADOS DE MAPEO ==========

    private void mapearRequestAEntidad(ClienteRequestDTO request, Cliente cliente) {
        cliente.setTipoCliente(request.getTipoCliente().toUpperCase());

        // ⭐⭐⭐ CORRECCIÓN PARA EMPRESAS ⭐⭐⭐
        if ("EMPRESA".equalsIgnoreCase(request.getTipoCliente())) {
            // Para empresas, usar la razón social como nombre completo
            cliente.setNombreCompleto(request.getRazonSocial());
            cliente.setNombre(request.getRazonSocial());
        } else {
            // Para personas, usar el nombre completo
            cliente.setNombreCompleto(request.getNombreCompleto());
            cliente.setNombre(request.getNombreCompleto());
        }

        cliente.setTipoDocumento(request.getTipoDocumento() != null ?
                request.getTipoDocumento().toUpperCase() : null);
        cliente.setNumeroDocumento(request.getNumeroDocumento());

        // Mapear DNI o RUC según el tipo de documento
        if ("DNI".equalsIgnoreCase(request.getTipoDocumento())) {
            cliente.setDni(request.getNumeroDocumento());
            cliente.setRuc(""); // Vacío para que no sea null
        } else if ("RUC".equalsIgnoreCase(request.getTipoDocumento())) {
            cliente.setRuc(request.getNumeroDocumento());
            cliente.setDni(""); // Vacío para que no sea null
        } else {
            // Para PASAPORTE o CARNET_EXTRANJERIA
            cliente.setDni("");
            cliente.setRuc("");
        }

        cliente.setTelefono(request.getTelefono());
        cliente.setEmail(request.getEmail());
        cliente.setDireccion(request.getDireccion());
        cliente.setDistrito(request.getDistrito());
        cliente.setProvincia(request.getProvincia());
        cliente.setDepartamento(request.getDepartamento());
        cliente.setRazonSocial(request.getRazonSocial());
        cliente.setNombreContacto(request.getNombreContacto());
        cliente.setNotas(request.getNotas());
    }

    private ClienteResponseDTO convertirAResponseDTO(Cliente cliente) {
        ClienteResponseDTO response = new ClienteResponseDTO();
        response.setId(cliente.getId());
        response.setTipoCliente(cliente.getTipoCliente());
        response.setNombreCompleto(cliente.getNombreCompleto());
        response.setTipoDocumento(cliente.getTipoDocumento());
        response.setNumeroDocumento(cliente.getNumeroDocumento());
        response.setTelefono(cliente.getTelefono());
        response.setEmail(cliente.getEmail());
        response.setDireccion(cliente.getDireccion());
        response.setDistrito(cliente.getDistrito());
        response.setProvincia(cliente.getProvincia());
        response.setDepartamento(cliente.getDepartamento());
        response.setRazonSocial(cliente.getRazonSocial());
        response.setNombreContacto(cliente.getNombreContacto());
        response.setNotas(cliente.getNotas());
        response.setActivo(cliente.getActivo());
        response.setFechaCreacion(cliente.getFechaCreacion());
        response.setFechaActualizacion(cliente.getFechaActualizacion());

        // Dirección completa concatenada
        response.setDireccionCompleta(construirDireccionCompleta(cliente));

        return response;
    }

    private String construirDireccionCompleta(Cliente cliente) {
        StringBuilder direccion = new StringBuilder();

        if (cliente.getDireccion() != null && !cliente.getDireccion().isEmpty()) {
            direccion.append(cliente.getDireccion());
        }
        if (cliente.getDistrito() != null && !cliente.getDistrito().isEmpty()) {
            if (direccion.length() > 0) direccion.append(", ");
            direccion.append(cliente.getDistrito());
        }
        if (cliente.getProvincia() != null && !cliente.getProvincia().isEmpty()) {
            if (direccion.length() > 0) direccion.append(", ");
            direccion.append(cliente.getProvincia());
        }
        if (cliente.getDepartamento() != null && !cliente.getDepartamento().isEmpty()) {
            if (direccion.length() > 0) direccion.append(", ");
            direccion.append(cliente.getDepartamento());
        }

        return direccion.toString();
    }
}