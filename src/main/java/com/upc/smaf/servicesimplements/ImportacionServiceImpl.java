package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.ImportacionRequestDTO;
import com.upc.smaf.dtos.response.ImportacionResponseDTO;
import com.upc.smaf.entities.*;
import com.upc.smaf.repositories.*;
import com.upc.smaf.serviceinterface.ImportacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode; // ✅ Importante para divisiones
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImportacionServiceImpl implements ImportacionService {

    private final ImportacionRepository importacionRepository;
    private final CompraRepository compraRepository;

    @Override
    public List<ImportacionResponseDTO> listarTodas() {
        return importacionRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ImportacionResponseDTO> listarPorEstado(EstadoImportacion estado) {
        return importacionRepository.findAll().stream()
                .filter(i -> i.getEstado() == estado)
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ImportacionResponseDTO obtenerPorId(Integer id) {
        Importacion imp = importacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Importación no encontrada"));
        return mapToResponseDTO(imp);
    }

    @Override
    public ImportacionResponseDTO obtenerPorCodigo(String codigo) {
        Importacion imp = importacionRepository.findByCodigoAgrupador(codigo)
                .orElseThrow(() -> new RuntimeException("Importación no encontrada: " + codigo));
        return mapToResponseDTO(imp);
    }

    @Override
    public ImportacionResponseDTO guardar(ImportacionRequestDTO request) {
        // Implementación opcional si la usas para crear desde cero sin compras
        return null;
    }

    @Override
    @Transactional
    public ImportacionResponseDTO actualizar(Integer id, ImportacionRequestDTO request) {
        Importacion imp = importacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No encontrada"));

        // 1. Actualizar Datos Generales y Enums
        if(request.getEstado() != null) {
            try { imp.setEstado(EstadoImportacion.valueOf(request.getEstado())); } catch (Exception e) {}
        }
        if(request.getTipoTransporte() != null) {
            try { imp.setTipoTransporte(TipoTransporte.valueOf(request.getTipoTransporte())); } catch (Exception e) {}
        }

        imp.setFechaEstimadaLlegada(request.getFechaEstimadaLlegada());

        // 2. Actualizar Datos de Aduanas
        imp.setNumeroDua(request.getNumeroDua());
        imp.setTrackingNumber(request.getTrackingNumber());
        imp.setAgenteAduanas(request.getAgenteAduanas());
        imp.setCanal(request.getCanal());

        // 3. Actualizar la "Bolsa" de Costos Globales
        imp.setTotalFleteInternacional(request.getTotalFleteInternacional() != null ? request.getTotalFleteInternacional() : BigDecimal.ZERO);
        imp.setTotalSeguro(request.getTotalSeguro() != null ? request.getTotalSeguro() : BigDecimal.ZERO);
        imp.setTotalGastosAduana(request.getTotalGastosAduana() != null ? request.getTotalGastosAduana() : BigDecimal.ZERO);
        imp.setTotalGastosAlmacen(request.getTotalGastosAlmacen() != null ? request.getTotalGastosAlmacen() : BigDecimal.ZERO);
        imp.setTotalTransporteLocal(request.getTotalTransporteLocal() != null ? request.getTotalTransporteLocal() : BigDecimal.ZERO);
        imp.setOtrosGastosGlobales(request.getOtrosGastosGlobales() != null ? request.getOtrosGastosGlobales() : BigDecimal.ZERO);

        // =================================================================================
        // 🚀 4. LÓGICA DE PRORRATEO (DISTRIBUCIÓN DE COSTOS POR PESO)
        // =================================================================================

        // A. Traer facturas vinculadas
        List<Compra> facturas = compraRepository.findByCodImportacion(imp.getCodigoAgrupador());

        // B. Calcular Peso Total de la carpeta
        BigDecimal pesoTotalCarpeta = facturas.stream()
                .map(c -> c.getPesoNetoKg() != null ? c.getPesoNetoKg() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        imp.setPesoTotalKg(pesoTotalCarpeta);

        // C. Recorrer facturas y asignar costos
        if (pesoTotalCarpeta.compareTo(BigDecimal.ZERO) > 0) {
            for (Compra factura : facturas) {
                // Peso de esta factura
                BigDecimal pesoFactura = factura.getPesoNetoKg() != null ? factura.getPesoNetoKg() : BigDecimal.ZERO;

                // FACTOR = PesoFactura / PesoTotal (Ej: 0.25 para el 25%)
                BigDecimal factor = pesoFactura.divide(pesoTotalCarpeta, 10, RoundingMode.HALF_UP);

                // Distribuir Costos Individuales
                BigDecimal fleteAsignado = imp.getTotalFleteInternacional().multiply(factor).setScale(2, RoundingMode.HALF_UP);
                BigDecimal seguroAsignado = imp.getTotalSeguro().multiply(factor).setScale(2, RoundingMode.HALF_UP);

                // Agrupamos Aduana + Almacén + Otros en un solo concepto de "Gastos Varios" para la factura
                // O si tienes campos separados en Compra, asígnalos uno por uno.
                BigDecimal gastosVariosGlobales = imp.getTotalGastosAduana()
                        .add(imp.getTotalGastosAlmacen())
                        .add(imp.getTotalTransporteLocal())
                        .add(imp.getOtrosGastosGlobales());

                BigDecimal gastosAsignados = gastosVariosGlobales.multiply(factor).setScale(2, RoundingMode.HALF_UP);

                // Guardar los parciales en la factura (asegúrate que la entidad Compra tenga estos setters)
                factura.setProrrateoFlete(fleteAsignado);
                factura.setProrrateoSeguro(seguroAsignado);
                factura.setProrrateoGastosAduanas(gastosAsignados);

                // CALCULAR COSTO REAL (LANDED) = FOB + Flete + Seguro + Gastos
                // Asumimos que factura.getTotal() es el FOB (Valor mercadería)
                BigDecimal costoLanded = factura.getTotal()
                        .add(fleteAsignado)
                        .add(seguroAsignado)
                        .add(gastosAsignados);

                factura.setCostoTotalImportacion(costoLanded);

                // Guardar cambios en la factura
                compraRepository.save(factura);
            }
        }

        Importacion saved = importacionRepository.save(imp);
        return mapToResponseDTO(saved);
    }

    @Override
    public void recalcularCostos(Integer id) {
        // Podrías llamar a actualizar(id, dtoActual) o extraer la lógica a un método privado
    }

    private ImportacionResponseDTO mapToResponseDTO(Importacion imp) {
        ImportacionResponseDTO dto = new ImportacionResponseDTO();
        dto.setId(imp.getId());
        dto.setCodigoAgrupador(imp.getCodigoAgrupador());
        dto.setEstado(imp.getEstado() != null ? imp.getEstado().name() : "ORDENADO");
        dto.setTipoTransporte(imp.getTipoTransporte() != null ? imp.getTipoTransporte().name() : null);

        dto.setFechaEstimadaLlegada(imp.getFechaEstimadaLlegada());
        dto.setNumeroDua(imp.getNumeroDua());
        dto.setTrackingNumber(imp.getTrackingNumber());
        dto.setAgenteAduanas(imp.getAgenteAduanas());
        dto.setCanal(imp.getCanal());

        dto.setTotalFleteInternacional(imp.getTotalFleteInternacional());
        dto.setTotalSeguro(imp.getTotalSeguro());
        dto.setTotalGastosAduana(imp.getTotalGastosAduana());
        dto.setTotalGastosAlmacen(imp.getTotalGastosAlmacen());
        dto.setTotalTransporteLocal(imp.getTotalTransporteLocal());
        dto.setOtrosGastosGlobales(imp.getOtrosGastosGlobales());

        dto.setSumaFobTotal(imp.getSumaFobTotal());
        dto.setPesoTotalKg(imp.getPesoTotalKg());

        // Facturas Vinculadas
        List<Compra> facturas = compraRepository.findByCodImportacion(imp.getCodigoAgrupador());

        List<ImportacionResponseDTO.CompraResumenDTO> resumenFacturas = facturas.stream().map(c -> {
            ImportacionResponseDTO.CompraResumenDTO res = new ImportacionResponseDTO.CompraResumenDTO();
            res.setId(c.getId());
            res.setSerie(c.getSerie());
            res.setNumero(c.getNumero());
            res.setNombreProveedor(c.getProveedor() != null ? c.getProveedor().getNombre() : "Sin Proveedor");
            res.setTotal(c.getTotal());
            res.setMoneda(c.getMoneda());
            res.setPesoNetoKg(c.getPesoNetoKg());
            return res;
        }).collect(Collectors.toList());

        dto.setFacturasComerciales(resumenFacturas);

        return dto;
    }
}