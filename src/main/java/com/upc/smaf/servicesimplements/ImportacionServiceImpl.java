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
import java.util.Map;
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
        return null;
    }

    @Override
    @Transactional
    public ImportacionResponseDTO actualizar(Integer id, ImportacionRequestDTO request) {
        Importacion imp = importacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Importación no encontrada"));

        // 1. ACTUALIZAR DATOS INFORMATIVOS
        if(request.getEstado() != null) try { imp.setEstado(EstadoImportacion.valueOf(request.getEstado())); } catch (Exception e) {}
        if(request.getTipoTransporte() != null) try { imp.setTipoTransporte(TipoTransporte.valueOf(request.getTipoTransporte())); } catch (Exception e) {}

        imp.setFechaEstimadaLlegada(request.getFechaEstimadaLlegada());
        imp.setFechaLlegadaReal(request.getFechaLlegadaReal());
        imp.setNumeroDua(request.getNumeroDua());
        imp.setTrackingNumber(request.getTrackingNumber());
        imp.setAgenteAduanas(request.getAgenteAduanas());
        imp.setCanal(request.getCanal());

        // 2. COSTOS GLOBALES
        imp.setCostoFlete(orZero(request.getCostoFlete()));
        imp.setCostoAlmacenajeCft(orZero(request.getCostoAlmacenajeCft()));
        imp.setCostoTransporteSjl(orZero(request.getCostoTransporteSjl()));
        imp.setCostoPersonalDescarga(orZero(request.getCostoPersonalDescarga()));
        imp.setCostoMontacarga(orZero(request.getCostoMontacarga()));

        imp.setCostoDesconsolidacion(orZero(request.getCostoDesconsolidacion()));

        imp.setCostoVistosBuenos(orZero(request.getCostoVistosBuenos()));
        imp.setCostoTransmision(orZero(request.getCostoTransmision()));
        imp.setCostoComisionAgencia(orZero(request.getCostoComisionAgencia()));
        imp.setCostoVobo(orZero(request.getCostoVobo()));
        imp.setCostoGastosOperativos(orZero(request.getCostoGastosOperativos()));
        imp.setCostoResguardo(orZero(request.getCostoResguardo()));

        imp.setCostoIgv(orZero(request.getCostoIgv()));
        imp.setCostoIpm(orZero(request.getCostoIpm()));
        imp.setCostoPercepcion(orZero(request.getCostoPercepcion()));

        imp.setCostoOtros1(orZero(request.getCostoOtros1()));
        imp.setCostoOtros2(orZero(request.getCostoOtros2()));
        imp.setCostoOtros3(orZero(request.getCostoOtros3()));
        imp.setCostoOtros4(orZero(request.getCostoOtros4()));

        // =================================================================================
        // 🚀 3. PRORRATEO NIVEL 1: DISTRIBUCIÓN A FACTURAS
        // =================================================================================

        List<Compra> facturas = compraRepository.findByCodImportacion(imp.getCodigoAgrupador());

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

        BigDecimal sumaAdValoremManual = BigDecimal.ZERO;

        for (Compra c : facturas) {
            if (c.getEstado() == EstadoCompra.ANULADA) continue;

            BigDecimal baseValor = c.getTotal();
            BigDecimal basePeso = orZero(c.getPesoNetoKg());
            BigDecimal baseCbm = orZero(c.getCbm());

            // --- CÁLCULOS DE PRORRATEO ---
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

            BigDecimal pOtros1 = prorratear(imp.getCostoOtros1(), totalFob, baseValor);
            BigDecimal pOtros2 = prorratear(imp.getCostoOtros2(), totalFob, baseValor);
            BigDecimal pOtros3 = prorratear(imp.getCostoOtros3(), totalFob, baseValor);
            BigDecimal pOtros4 = prorratear(imp.getCostoOtros4(), totalFob, baseValor);

            // Ad Valorem Manual
            BigDecimal pAdv = BigDecimal.ZERO;
            Map<Integer, BigDecimal> mapAdv = request.getAdValoremPorFactura();

            if (mapAdv != null && mapAdv.containsKey(c.getId())) {
                pAdv = mapAdv.get(c.getId());
                if (pAdv == null) pAdv = BigDecimal.ZERO;
            } else {
                pAdv = orZero(c.getProAdv());
            }
            sumaAdValoremManual = sumaAdValoremManual.add(pAdv);

            // =====================================================================
            // 🛑 CORRECCIÓN: GUARDAR EN LOS CAMPOS INDIVIDUALES (SETTERS)
            // =====================================================================

            // Grupo Volumen
            c.setProFlete(pFlete);
            c.setProAlmacenaje(pAlmacen);
            c.setProTransporte(pTransporte);
            c.setProPersonalDescarga(pDescarga); // ✅ Antes faltaba
            c.setProMontacarga(pMontacarga);     // ✅ Antes faltaba

            // Grupo Peso
            c.setProDesconsolidacion(pDesconsol);

            // Grupo Valor / Aduanas
            c.setProVistosBuenos(pVistos);       // ✅ Nuevo
            c.setProTransmision(pTransm);        // ✅ Nuevo
            c.setProComisionAgencia(pAgencia);   // ✅ Nuevo
            c.setProVobo(pVobo);                 // ✅ Nuevo
            c.setProGastosOperativos(pGastosOp); // ✅ Nuevo
            c.setProResguardo(pResguardo);       // ✅ Nuevo

            // Grupo Impuestos
            c.setProAdv(pAdv);
            c.setProIgv(pIgv);                   // ✅ Nuevo
            c.setProIpm(pIpm);                   // ✅ Nuevo
            c.setProPercepcion(pPercep);         // ✅ Nuevo

            // Grupo Otros
            c.setProOtros1(pOtros1);             // ✅ Nuevo
            c.setProOtros2(pOtros2);             // ✅ Nuevo
            c.setProOtros3(pOtros3);             // ✅ Nuevo
            c.setProOtros4(pOtros4);             // ✅ Nuevo

            // --- MANTENER CAMPOS ANTIGUOS (SOLO POR SEGURIDAD/COMPATIBILIDAD) ---
            c.setProCargaDescarga(pDescarga.add(pMontacarga));
            c.setProGastosAduaneros(pVistos.add(pTransm).add(pAgencia).add(pVobo).add(pGastosOp));
            c.setProSeguroResguardo(pResguardo);
            c.setProImpuestos(pIgv.add(pIpm).add(pPercep));
            c.setProOtrosGastos(pOtros1.add(pOtros2).add(pOtros3).add(pOtros4));

            // Costo Landed Total Factura
            BigDecimal costoLandedFactura = baseValor
                    .add(pFlete).add(pAlmacen).add(pTransporte).add(pDescarga).add(pMontacarga)
                    .add(pDesconsol)
                    .add(pVistos).add(pTransm).add(pAgencia).add(pVobo).add(pGastosOp)
                    .add(pResguardo)
                    .add(pIgv).add(pIpm).add(pPercep)
                    .add(pAdv)
                    .add(pOtros1).add(pOtros2).add(pOtros3).add(pOtros4);

            c.setCostoTotalImportacion(costoLandedFactura);

            // 🚀 4. PRORRATEO NIVEL 2: ITEMS
            distribuirCostosAItems(c);

            compraRepository.save(c);
        }

        imp.setCostoAdv(sumaAdValoremManual);
        Importacion saved = importacionRepository.save(imp);

        return mapToResponseDTO(saved);
    }

    // ✅ PRORRATEO DE ÍTEMS (NIVEL 2)
    private void distribuirCostosAItems(Compra c) {
        List<CompraDetalle> detalles = c.getDetalles();
        if (detalles == null || detalles.isEmpty()) return;

        BigDecimal totalFobFactura = c.getTotal();
        if (totalFobFactura == null || totalFobFactura.compareTo(BigDecimal.ZERO) == 0) return;

        BigDecimal totalSobrecostos = c.getCostoTotalImportacion().subtract(totalFobFactura);

        for (CompraDetalle item : detalles) {
            BigDecimal importeFobItem = item.getImporteTotal();
            if (importeFobItem == null) importeFobItem = BigDecimal.ZERO;

            BigDecimal factor = importeFobItem.divide(totalFobFactura, 10, RoundingMode.HALF_UP);

            BigDecimal sobrecostoItem = totalSobrecostos.multiply(factor);

            BigDecimal costoTotalLanded = importeFobItem.add(sobrecostoItem);

            BigDecimal costoUnitarioLanded = BigDecimal.ZERO;
            if (item.getCantidad() != null && item.getCantidad() > 0) {
                costoUnitarioLanded = costoTotalLanded.divide(new BigDecimal(item.getCantidad()), 4, RoundingMode.HALF_UP);
            }

            item.setCostoTotalLanded(costoTotalLanded);
            item.setCostoUnitarioLanded(costoUnitarioLanded);
        }
    }

    @Override
    public void recalcularCostos(Integer id) {
    }

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
    // 📄 MAPEO A DTO (CON DESGLOSE TOTAL DE ÍTEMS)
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

        // TOTALES
        dto.setSumaFobTotal(imp.getSumaFobTotal());
        dto.setPesoTotalKg(imp.getPesoTotalKg());
        dto.setCbmTotal(imp.getCbmTotal());

        // COSTOS
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

        // FACTURAS
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

            // BASES
            BigDecimal totalValor = imp.getSumaFobTotal();
            BigDecimal totalPeso = imp.getPesoTotalKg();
            BigDecimal totalCbm = imp.getCbmTotal();

            BigDecimal baseValor = c.getTotal();
            BigDecimal basePeso = orZero(c.getPesoNetoKg());
            BigDecimal baseCbm = orZero(c.getCbm());

            // PRORRATEO VISUAL FACTURA
            r.setProFlete(prorratear(imp.getCostoFlete(), totalCbm, baseCbm));
            r.setProAlmacenaje(prorratear(imp.getCostoAlmacenajeCft(), totalCbm, baseCbm));
            r.setProTransporte(prorratear(imp.getCostoTransporteSjl(), totalCbm, baseCbm));
            r.setProPersonalDescarga(prorratear(imp.getCostoPersonalDescarga(), totalCbm, baseCbm));
            r.setProMontacarga(prorratear(imp.getCostoMontacarga(), totalCbm, baseCbm));

            r.setProDesconsolidacion(prorratear(imp.getCostoDesconsolidacion(), totalPeso, basePeso));

            r.setProVistosBuenos(prorratear(imp.getCostoVistosBuenos(), totalValor, baseValor));
            r.setProTransmision(prorratear(imp.getCostoTransmision(), totalValor, baseValor));
            r.setProComisionAgencia(prorratear(imp.getCostoComisionAgencia(), totalValor, baseValor));
            r.setProVobo(prorratear(imp.getCostoVobo(), totalValor, baseValor));
            r.setProGastosOperativos(prorratear(imp.getCostoGastosOperativos(), totalValor, baseValor));
            r.setProResguardo(prorratear(imp.getCostoResguardo(), totalValor, baseValor));

            r.setProIgv(prorratear(imp.getCostoIgv(), totalValor, baseValor));
            r.setProIpm(prorratear(imp.getCostoIpm(), totalValor, baseValor));
            r.setProPercepcion(prorratear(imp.getCostoPercepcion(), totalValor, baseValor));

            r.setProAdv(orZero(c.getProAdv()));

            r.setProOtros1(prorratear(imp.getCostoOtros1(), totalValor, baseValor));
            r.setProOtros2(prorratear(imp.getCostoOtros2(), totalValor, baseValor));
            r.setProOtros3(prorratear(imp.getCostoOtros3(), totalValor, baseValor));
            r.setProOtros4(prorratear(imp.getCostoOtros4(), totalValor, baseValor));

            r.setCostoTotalImportacion(c.getCostoTotalImportacion());

            // ✅ ITEMS (NIVEL 2) CON DESGLOSE COMPLETO POR COLUMNA
            if (c.getDetalles() != null) {
                List<ImportacionResponseDTO.DetalleItemDTO> itemsDto = c.getDetalles().stream().map(d -> {
                    ImportacionResponseDTO.DetalleItemDTO item = new ImportacionResponseDTO.DetalleItemDTO();

                    item.setNombreProducto(d.getProducto().getNombre());
                    item.setCantidad(new BigDecimal(d.getCantidad()));
                    item.setPrecioUnitarioFob(d.getPrecioUnitario());
                    item.setImporteFob(d.getImporteTotal());

                    // Factor de Participación
                    BigDecimal factor = BigDecimal.ZERO;
                    if (c.getTotal() != null && c.getTotal().compareTo(BigDecimal.ZERO) > 0 && d.getImporteTotal() != null) {
                        factor = d.getImporteTotal().divide(c.getTotal(), 10, RoundingMode.HALF_UP);
                    }
                    item.setFactorParticipacion(factor);

                    // ⬇️ AQUÍ ESTÁ LA MAGIA: Calculamos cada columna por separado usando los campos de Compra
                    item.setItemFlete(orZero(c.getProFlete()).multiply(factor));
                    item.setItemAlmacenaje(orZero(c.getProAlmacenaje()).multiply(factor));
                    item.setItemTransporte(orZero(c.getProTransporte()).multiply(factor));
                    item.setItemDescarga(orZero(c.getProPersonalDescarga()).multiply(factor));
                    item.setItemMontacarga(orZero(c.getProMontacarga()).multiply(factor));

                    item.setItemDesconsolidacion(orZero(c.getProDesconsolidacion()).multiply(factor));

                    item.setItemVistosBuenos(orZero(c.getProVistosBuenos()).multiply(factor));
                    item.setItemTransmision(orZero(c.getProTransmision()).multiply(factor));
                    item.setItemAgente(orZero(c.getProComisionAgencia()).multiply(factor));
                    item.setItemVobo(orZero(c.getProVobo()).multiply(factor));
                    item.setItemGastosOp(orZero(c.getProGastosOperativos()).multiply(factor));
                    item.setItemResguardo(orZero(c.getProResguardo()).multiply(factor));

                    item.setItemAdv(orZero(c.getProAdv()).multiply(factor));
                    item.setItemIgv(orZero(c.getProIgv()).multiply(factor));
                    item.setItemIpm(orZero(c.getProIpm()).multiply(factor));
                    item.setItemPercepcion(orZero(c.getProPercepcion()).multiply(factor));

                    item.setItemOtros1(orZero(c.getProOtros1()).multiply(factor));
                    item.setItemOtros2(orZero(c.getProOtros2()).multiply(factor));

                    item.setCostoTotalLanded(d.getCostoTotalLanded());
                    item.setCostoUnitarioLanded(d.getCostoUnitarioLanded());

                    return item;
                }).collect(Collectors.toList());

                r.setItems(itemsDto);
            }

            return r;
        }).collect(Collectors.toList());

        dto.setFacturasComerciales(resumen);
        return dto;
    }
}