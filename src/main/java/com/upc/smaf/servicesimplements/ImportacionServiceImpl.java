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
import java.math.RoundingMode;
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
        // Implementación opcional
        return null;
    }

    @Override
    @Transactional
    public ImportacionResponseDTO actualizar(Integer id, ImportacionRequestDTO request) {
        Importacion imp = importacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Importación no encontrada"));

        // 1. ACTUALIZAR DATOS INFORMATIVOS Y LOGÍSTICOS
        if(request.getEstado() != null) try { imp.setEstado(EstadoImportacion.valueOf(request.getEstado())); } catch (Exception e) {}
        if(request.getTipoTransporte() != null) try { imp.setTipoTransporte(TipoTransporte.valueOf(request.getTipoTransporte())); } catch (Exception e) {}

        imp.setFechaEstimadaLlegada(request.getFechaEstimadaLlegada());
        imp.setFechaLlegadaReal(request.getFechaLlegadaReal());
        imp.setNumeroDua(request.getNumeroDua());
        imp.setTrackingNumber(request.getTrackingNumber());
        imp.setAgenteAduanas(request.getAgenteAduanas());
        imp.setCanal(request.getCanal());

        // 2. ACTUALIZAR LOS INPUTS DE COSTOS GLOBALES (Lo que escribe el usuario)

        // --- Grupo Volumen ---
        imp.setCostoFlete(orZero(request.getCostoFlete()));
        imp.setCostoAlmacenajeCft(orZero(request.getCostoAlmacenajeCft()));
        imp.setCostoTransporteSjl(orZero(request.getCostoTransporteSjl()));
        imp.setCostoPersonalDescarga(orZero(request.getCostoPersonalDescarga()));
        imp.setCostoMontacarga(orZero(request.getCostoMontacarga()));

        // --- Grupo Peso ---
        imp.setCostoDesconsolidacion(orZero(request.getCostoDesconsolidacion()));

        // --- Grupo Valor ---
        imp.setCostoVistosBuenos(orZero(request.getCostoVistosBuenos()));
        imp.setCostoTransmision(orZero(request.getCostoTransmision()));
        imp.setCostoComisionAgencia(orZero(request.getCostoComisionAgencia()));
        imp.setCostoVobo(orZero(request.getCostoVobo()));
        imp.setCostoGastosOperativos(orZero(request.getCostoGastosOperativos()));
        imp.setCostoResguardo(orZero(request.getCostoResguardo()));

        // --- Impuestos y Otros ---
        imp.setCostoIgv(orZero(request.getCostoIgv()));
        imp.setCostoIpm(orZero(request.getCostoIpm()));
        imp.setCostoPercepcion(orZero(request.getCostoPercepcion()));
        imp.setCostoAdv(orZero(request.getCostoAdv()));

        imp.setCostoOtros1(orZero(request.getCostoOtros1()));
        imp.setCostoOtros2(orZero(request.getCostoOtros2()));
        imp.setCostoOtros3(orZero(request.getCostoOtros3()));
        imp.setCostoOtros4(orZero(request.getCostoOtros4()));

        // =================================================================================
        // 🚀 3. LÓGICA DE CÁLCULO Y PERSISTENCIA (AGRUPADA EN BD)
        // =================================================================================

        List<Compra> facturas = compraRepository.findByCodImportacion(imp.getCodigoAgrupador());

        // A. RECALCULAR TOTALES DE LA CARPETA (Base Total)
        BigDecimal totalFob = BigDecimal.ZERO;
        BigDecimal totalPeso = BigDecimal.ZERO;
        BigDecimal totalCbm = BigDecimal.ZERO;

        for (Compra c : facturas) {
            if (c.getEstado() != EstadoCompra.ANULADA) {
                totalFob = totalFob.add(c.getTotal());
                totalPeso = totalPeso.add(orZero(c.getPesoNetoKg()));
                totalCbm = totalCbm.add(orZero(c.getCbm()));
            }
        }

        imp.setSumaFobTotal(totalFob);
        imp.setPesoTotalKg(totalPeso);
        imp.setCbmTotal(totalCbm);

        // B. DISTRIBUIR COSTOS A CADA FACTURA (Persistencia en BD)
        for (Compra c : facturas) {
            if (c.getEstado() == EstadoCompra.ANULADA) continue;

            BigDecimal baseValor = c.getTotal();
            BigDecimal basePeso = orZero(c.getPesoNetoKg());
            BigDecimal baseCbm = orZero(c.getCbm());

            // 1. Calculamos parciales para agrupar y guardar en BD
            BigDecimal pFlete = prorratear(imp.getCostoFlete(), totalCbm, baseCbm);
            BigDecimal pAlmacen = prorratear(imp.getCostoAlmacenajeCft(), totalCbm, baseCbm);
            BigDecimal pTransporte = prorratear(imp.getCostoTransporteSjl(), totalCbm, baseCbm);
            BigDecimal pDescarga = prorratear(imp.getCostoPersonalDescarga(), totalCbm, baseCbm);
            BigDecimal pMontacarga = prorratear(imp.getCostoMontacarga(), totalCbm, baseCbm);

            BigDecimal pDesconsol = prorratear(imp.getCostoDesconsolidacion(), totalPeso, basePeso);

            BigDecimal pVistos = prorratear(imp.getCostoVistosBuenos(), totalFob, baseValor);
            BigDecimal pTransm = prorratear(imp.getCostoTransmision(), totalFob, baseValor);
            BigDecimal pAgencia = prorratear(imp.getCostoComisionAgencia(), totalFob, baseValor);
            BigDecimal pVobo = prorratear(imp.getCostoVobo(), totalFob, baseValor);
            BigDecimal pGastosOp = prorratear(imp.getCostoGastosOperativos(), totalFob, baseValor);
            BigDecimal pResguardo = prorratear(imp.getCostoResguardo(), totalFob, baseValor);

            BigDecimal pIgv = prorratear(imp.getCostoIgv(), totalFob, baseValor);
            BigDecimal pIpm = prorratear(imp.getCostoIpm(), totalFob, baseValor);
            BigDecimal pPercep = prorratear(imp.getCostoPercepcion(), totalFob, baseValor);
            BigDecimal pAdv = prorratear(imp.getCostoAdv(), totalFob, baseValor);

            BigDecimal pOtros1 = prorratear(imp.getCostoOtros1(), totalFob, baseValor);
            BigDecimal pOtros2 = prorratear(imp.getCostoOtros2(), totalFob, baseValor);
            BigDecimal pOtros3 = prorratear(imp.getCostoOtros3(), totalFob, baseValor);
            BigDecimal pOtros4 = prorratear(imp.getCostoOtros4(), totalFob, baseValor);

            // 2. Guardamos en la entidad COMPRA (Agrupados para no saturar la BD)
            c.setProFlete(pFlete);
            c.setProAlmacenaje(pAlmacen);
            c.setProTransporte(pTransporte);
            c.setProCargaDescarga(pDescarga.add(pMontacarga)); // Agrupado
            c.setProDesconsolidacion(pDesconsol);

            c.setProGastosAduaneros(pVistos.add(pTransm).add(pAgencia).add(pVobo).add(pGastosOp)); // Agrupado
            c.setProSeguroResguardo(pResguardo);
            c.setProImpuestos(pIgv.add(pIpm).add(pPercep)); // Agrupado
            c.setProOtrosGastos(pAdv.add(pOtros1).add(pOtros2).add(pOtros3).add(pOtros4)); // Agrupado

            // 3. Costo Landed (Suma Total Real)
            BigDecimal costoLanded = baseValor
                    .add(pFlete).add(pAlmacen).add(pTransporte).add(pDescarga).add(pMontacarga)
                    .add(pDesconsol)
                    .add(pVistos).add(pTransm).add(pAgencia).add(pVobo).add(pGastosOp)
                    .add(pResguardo)
                    .add(pIgv).add(pIpm).add(pPercep)
                    .add(pAdv).add(pOtros1).add(pOtros2).add(pOtros3).add(pOtros4);

            c.setCostoTotalImportacion(costoLanded);

            compraRepository.save(c);
        }

        Importacion saved = importacionRepository.save(imp);
        return mapToResponseDTO(saved);
    }

    @Override
    public void recalcularCostos(Integer id) {
        // Lógica opcional
    }

    // Helper matemático
    private BigDecimal prorratear(BigDecimal costoGlobal, BigDecimal baseTotal, BigDecimal baseIndividual) {
        if (costoGlobal == null || costoGlobal.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        if (baseTotal == null || baseTotal.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        if (baseIndividual == null) return BigDecimal.ZERO;

        return costoGlobal
                .divide(baseTotal, 10, RoundingMode.HALF_UP)
                .multiply(baseIndividual)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal orZero(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }

    // =================================================================================
    // 📄 MAPEO A DTO (AQUÍ DESAGRUPAMOS PARA QUE EL FRONT VEA TODO DETALLADO)
    // =================================================================================
    private ImportacionResponseDTO mapToResponseDTO(Importacion imp) {
        ImportacionResponseDTO dto = new ImportacionResponseDTO();
        dto.setId(imp.getId());
        dto.setCodigoAgrupador(imp.getCodigoAgrupador());
        dto.setEstado(imp.getEstado() != null ? imp.getEstado().name() : "ORDENADO");
        dto.setTipoTransporte(imp.getTipoTransporte() != null ? imp.getTipoTransporte().name() : null);

        dto.setFechaEstimadaLlegada(imp.getFechaEstimadaLlegada());
        dto.setFechaLlegadaReal(imp.getFechaLlegadaReal());
        dto.setNumeroDua(imp.getNumeroDua());
        dto.setTrackingNumber(imp.getTrackingNumber());
        dto.setAgenteAduanas(imp.getAgenteAduanas());
        dto.setCanal(imp.getCanal());

        // TOTALES DE LA CARPETA
        dto.setSumaFobTotal(imp.getSumaFobTotal());
        dto.setPesoTotalKg(imp.getPesoTotalKg());
        dto.setCbmTotal(imp.getCbmTotal());

        // MAPEAR COSTOS GLOBALES
        dto.setCostoFlete(imp.getCostoFlete());
        dto.setCostoAlmacenajeCft(imp.getCostoAlmacenajeCft());
        dto.setCostoTransporteSjl(imp.getCostoTransporteSjl());
        dto.setCostoPersonalDescarga(imp.getCostoPersonalDescarga());
        dto.setCostoMontacarga(imp.getCostoMontacarga());
        dto.setCostoDesconsolidacion(imp.getCostoDesconsolidacion());
        dto.setCostoVistosBuenos(imp.getCostoVistosBuenos());
        dto.setCostoTransmision(imp.getCostoTransmision());
        dto.setCostoComisionAgencia(imp.getCostoComisionAgencia());
        dto.setCostoVobo(imp.getCostoVobo());
        dto.setCostoGastosOperativos(imp.getCostoGastosOperativos());
        dto.setCostoResguardo(imp.getCostoResguardo());
        dto.setCostoIgv(imp.getCostoIgv());
        dto.setCostoIpm(imp.getCostoIpm());
        dto.setCostoPercepcion(imp.getCostoPercepcion());
        dto.setCostoAdv(imp.getCostoAdv());
        dto.setCostoOtros1(imp.getCostoOtros1());
        dto.setCostoOtros2(imp.getCostoOtros2());
        dto.setCostoOtros3(imp.getCostoOtros3());
        dto.setCostoOtros4(imp.getCostoOtros4());

        // MAPEAR FACTURAS (CALCULANDO AL VUELO EL DETALLE DESAGRUPADO)
        List<Compra> facturas = compraRepository.findByCodImportacion(imp.getCodigoAgrupador());

        List<ImportacionResponseDTO.CompraResumenDTO> resumen = facturas.stream().map(c -> {
            ImportacionResponseDTO.CompraResumenDTO r = new ImportacionResponseDTO.CompraResumenDTO();
            r.setId(c.getId());
            r.setSerie(c.getSerie());
            r.setNumero(c.getNumero());
            r.setNombreProveedor(c.getProveedor() != null ? c.getProveedor().getNombre() : "Sin Proveedor");
            r.setTotal(c.getTotal());
            r.setMoneda(c.getMoneda());
            r.setPesoNetoKg(c.getPesoNetoKg());
            r.setCbm(c.getCbm());

            // BASES DE CÁLCULO
            BigDecimal totalValor = imp.getSumaFobTotal();
            BigDecimal totalPeso = imp.getPesoTotalKg();
            BigDecimal totalCbm = imp.getCbmTotal();

            BigDecimal baseValor = c.getTotal();
            BigDecimal basePeso = orZero(c.getPesoNetoKg());
            BigDecimal baseCbm = orZero(c.getCbm());

            // --- CÁLCULO AL VUELO PARA MOSTRAR LAS 16+ COLUMNAS ---

            // Grupo Volumen
            r.setProFlete(prorratear(imp.getCostoFlete(), totalCbm, baseCbm));
            r.setProAlmacenaje(prorratear(imp.getCostoAlmacenajeCft(), totalCbm, baseCbm));
            r.setProTransporte(prorratear(imp.getCostoTransporteSjl(), totalCbm, baseCbm));
            r.setProPersonalDescarga(prorratear(imp.getCostoPersonalDescarga(), totalCbm, baseCbm));
            r.setProMontacarga(prorratear(imp.getCostoMontacarga(), totalCbm, baseCbm));

            // Grupo Peso
            r.setProDesconsolidacion(prorratear(imp.getCostoDesconsolidacion(), totalPeso, basePeso));

            // Grupo Valor
            r.setProVistosBuenos(prorratear(imp.getCostoVistosBuenos(), totalValor, baseValor));
            r.setProTransmision(prorratear(imp.getCostoTransmision(), totalValor, baseValor));
            r.setProComisionAgencia(prorratear(imp.getCostoComisionAgencia(), totalValor, baseValor));
            r.setProVobo(prorratear(imp.getCostoVobo(), totalValor, baseValor));
            r.setProGastosOperativos(prorratear(imp.getCostoGastosOperativos(), totalValor, baseValor));
            r.setProResguardo(prorratear(imp.getCostoResguardo(), totalValor, baseValor));

            // Impuestos
            r.setProAdv(prorratear(imp.getCostoAdv(), totalValor, baseValor));
            r.setProIgv(prorratear(imp.getCostoIgv(), totalValor, baseValor));
            r.setProIpm(prorratear(imp.getCostoIpm(), totalValor, baseValor));
            r.setProPercepcion(prorratear(imp.getCostoPercepcion(), totalValor, baseValor));

            // Otros
            r.setProOtros1(prorratear(imp.getCostoOtros1(), totalValor, baseValor));
            r.setProOtros2(prorratear(imp.getCostoOtros2(), totalValor, baseValor));
            r.setProOtros3(prorratear(imp.getCostoOtros3(), totalValor, baseValor));
            r.setProOtros4(prorratear(imp.getCostoOtros4(), totalValor, baseValor));

            // Costo Final (Este sí viene de la BD)
            r.setCostoTotalImportacion(c.getCostoTotalImportacion());

            return r;
        }).collect(Collectors.toList());

        dto.setFacturasComerciales(resumen);

        return dto;
    }
}