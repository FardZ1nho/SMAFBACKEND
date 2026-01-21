package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.ImportacionRequestDTO;
import com.upc.smaf.dtos.response.CompraResponseDTO;
import com.upc.smaf.dtos.response.ImportacionResponseDTO;
import com.upc.smaf.entities.Compra;
import com.upc.smaf.entities.Importacion;
import com.upc.smaf.repositories.ImportacionRepository;
import com.upc.smaf.serviceinterface.ImportacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImportacionServiceImpl implements ImportacionService {

    private final ImportacionRepository importacionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ImportacionResponseDTO> listarTodas() {
        return importacionRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImportacionResponseDTO> listarPorEstado(com.upc.smaf.entities.EstadoImportacion estado) {
        return importacionRepository.findByEstado(estado).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImportacionResponseDTO obtenerPorId(Integer id) {
        Importacion imp = importacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Importación no encontrada ID: " + id));
        return mapToDTO(imp);
    }

    @Override
    @Transactional
    public ImportacionResponseDTO actualizarImportacion(Integer id, ImportacionRequestDTO request) {
        Importacion imp = importacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Importación no encontrada"));

        // --- 1. ACTUALIZAR SEGUIMIENTO Y FECHAS ---
        if(request.getNumeroDua() != null) imp.setNumeroDua(request.getNumeroDua());
        if(request.getTrackingNumber() != null) imp.setTrackingNumber(request.getTrackingNumber());
        if(request.getFechaEstimadaLlegada() != null) imp.setFechaEstimadaLlegada(request.getFechaEstimadaLlegada());
        if(request.getFechaNacionalizacion() != null) imp.setFechaNacionalizacion(request.getFechaNacionalizacion());

        // --- 2. ACTUALIZAR LOGÍSTICA (NUEVO) ---
        if(request.getPaisOrigen() != null) imp.setPaisOrigen(request.getPaisOrigen());
        if(request.getPuertoEmbarque() != null) imp.setPuertoEmbarque(request.getPuertoEmbarque());
        if(request.getPuertoLlegada() != null) imp.setPuertoLlegada(request.getPuertoLlegada());
        if(request.getIncoterm() != null) imp.setIncoterm(request.getIncoterm());
        if(request.getTipoTransporte() != null) imp.setTipoTransporte(request.getTipoTransporte());
        if(request.getNavieraAerolinea() != null) imp.setNavieraAerolinea(request.getNavieraAerolinea());
        if(request.getNumeroContenedor() != null) imp.setNumeroContenedor(request.getNumeroContenedor());

        // --- 3. ACTUALIZAR COSTOS ---
        // Se actualizan si vienen en el request (si es null, se mantiene el anterior)
        if(request.getCostoFlete() != null) imp.setCostoFlete(request.getCostoFlete());
        if(request.getCostoSeguro() != null) imp.setCostoSeguro(request.getCostoSeguro());
        if(request.getImpuestosAduanas() != null) imp.setImpuestosAduanas(request.getImpuestosAduanas());
        if(request.getGastosOperativos() != null) imp.setGastosOperativos(request.getGastosOperativos());

        // Nuevo costo nacional
        if(request.getCostoTransporteLocal() != null) imp.setCostoTransporteLocal(request.getCostoTransporteLocal());

        // --- 4. ACTUALIZAR ESTADO ---
        if(request.getEstado() != null) imp.setEstado(request.getEstado());

        return mapToDTO(importacionRepository.save(imp));
    }

    // --- MAPEO MANUAL ENTITY -> DTO ---
    private ImportacionResponseDTO mapToDTO(Importacion entity) {
        ImportacionResponseDTO dto = new ImportacionResponseDTO();
        dto.setId(entity.getId());

        // Seguimiento
        dto.setEstado(entity.getEstado());
        dto.setNumeroDua(entity.getNumeroDua());
        dto.setTrackingNumber(entity.getTrackingNumber());
        dto.setFechaEstimadaLlegada(entity.getFechaEstimadaLlegada());
        dto.setFechaNacionalizacion(entity.getFechaNacionalizacion());

        // Logística (NUEVO)
        dto.setPaisOrigen(entity.getPaisOrigen());
        dto.setPuertoEmbarque(entity.getPuertoEmbarque());
        dto.setPuertoLlegada(entity.getPuertoLlegada());
        dto.setIncoterm(entity.getIncoterm());
        dto.setTipoTransporte(entity.getTipoTransporte());
        dto.setNavieraAerolinea(entity.getNavieraAerolinea());
        dto.setNumeroContenedor(entity.getNumeroContenedor());

        // Costos
        dto.setCostoFlete(entity.getCostoFlete());
        dto.setCostoSeguro(entity.getCostoSeguro());
        dto.setImpuestosAduanas(entity.getImpuestosAduanas());
        dto.setGastosOperativos(entity.getGastosOperativos());
        dto.setCostoTransporteLocal(entity.getCostoTransporteLocal()); // Nuevo

        dto.setFechaCreacion(entity.getFechaCreacion());

        // Mapear Compra Básica (Datos necesarios para la tabla visual)
        if(entity.getCompra() != null) {
            Compra c = entity.getCompra();
            CompraResponseDTO cDto = new CompraResponseDTO();
            cDto.setId(c.getId());
            cDto.setSerie(c.getSerie());
            cDto.setNumero(c.getNumero());
            cDto.setFechaEmision(c.getFechaEmision());
            cDto.setTotal(c.getTotal());
            cDto.setMoneda(c.getMoneda());

            if(c.getProveedor() != null) {
                cDto.setNombreProveedor(c.getProveedor().getNombre());
            }
            dto.setCompra(cDto);
        }

        return dto;
    }
}