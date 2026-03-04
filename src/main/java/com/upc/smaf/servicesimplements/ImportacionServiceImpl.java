package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.ImportacionRequestDTO;
import com.upc.smaf.dtos.request.RecepcionItemDTO;
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
    private final CompraDetalleRepository compraDetalleRepository;
    private final ProductoRepository productoRepository;

    @Override
    public List<ImportacionResponseDTO> listarTodas() {
        return importacionRepository.findAll().stream()
                // ✅ MAGIA: Ocultar carpetas fantasma sin nombre
                .filter(i -> i.getCodigoAgrupador() != null && !i.getCodigoAgrupador().trim().isEmpty())
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ImportacionResponseDTO> listarPorEstado(EstadoImportacion estado) {
        return importacionRepository.findAll().stream()
                .filter(i -> i.getEstado() == estado)
                // ✅ MAGIA: Ocultar carpetas fantasma sin nombre
                .filter(i -> i.getCodigoAgrupador() != null && !i.getCodigoAgrupador().trim().isEmpty())
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ImportacionResponseDTO obtenerPorId(Integer id) {
        Importacion imp = importacionRepository.findById(id).orElseThrow(() -> new RuntimeException("Importación no encontrada"));
        return mapToResponseDTO(imp);
    }

    @Override
    public ImportacionResponseDTO obtenerPorCodigo(String codigo) {
        Importacion imp = importacionRepository.findByCodigoAgrupador(codigo).orElseThrow(() -> new RuntimeException("Importación no encontrada: " + codigo));
        return mapToResponseDTO(imp);
    }

    @Override
    public ImportacionResponseDTO guardar(ImportacionRequestDTO request) {
        return null;
    }

    @Override
    @Transactional
    public ImportacionResponseDTO actualizar(Integer id, ImportacionRequestDTO request) {
        Importacion imp = importacionRepository.findById(id).orElseThrow(() -> new RuntimeException("Importación no encontrada"));

        if(request.getEstado() != null) try { imp.setEstado(EstadoImportacion.valueOf(request.getEstado())); } catch (Exception e) {}
        if(request.getTipoTransporte() != null) try { imp.setTipoTransporte(TipoTransporte.valueOf(request.getTipoTransporte())); } catch (Exception e) {}

        imp.setFechaEstimadaLlegada(request.getFechaEstimadaLlegada());
        imp.setFechaLlegadaReal(request.getFechaLlegadaReal());
        imp.setNumeroDua(request.getNumeroDua());
        imp.setTrackingNumber(request.getTrackingNumber());
        imp.setAgenteAduanas(request.getAgenteAduanas());
        imp.setCanal(request.getCanal());

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

        List<Compra> facturas = compraRepository.findByCodImportacion(imp.getCodigoAgrupador());
        Map<Integer, BigDecimal> advMap = request.getAdValoremPorItem();
        BigDecimal sumaAdValoremTotalImportacion = BigDecimal.ZERO;

        for (Compra c : facturas) {
            if (c.getEstado() == EstadoCompra.ANULADA) continue;
            BigDecimal sumaAdValoremFactura = BigDecimal.ZERO;
            if (c.getDetalles() != null) {
                for (CompraDetalle detalle : c.getDetalles()) {
                    if (advMap != null && advMap.containsKey(detalle.getId())) {
                        detalle.setAdValoremItem(orZero(advMap.get(detalle.getId())));
                    } else if (detalle.getAdValoremItem() == null) {
                        detalle.setAdValoremItem(BigDecimal.ZERO);
                    }
                    sumaAdValoremFactura = sumaAdValoremFactura.add(detalle.getAdValoremItem());
                    compraDetalleRepository.save(detalle);
                }
            }
            c.setProAdv(sumaAdValoremFactura);
            sumaAdValoremTotalImportacion = sumaAdValoremTotalImportacion.add(sumaAdValoremFactura);
        }

        imp.setCostoAdv(sumaAdValoremTotalImportacion);

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

        for (Compra c : facturas) {
            if (c.getEstado() == EstadoCompra.ANULADA) continue;

            BigDecimal baseValor = c.getTotal();
            BigDecimal basePeso = orZero(c.getPesoNetoKg());
            BigDecimal baseCbm = orZero(c.getCbm());

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

            c.setProFlete(pFlete);
            c.setProAlmacenaje(pAlmacen);
            c.setProTransporte(pTransporte);
            c.setProPersonalDescarga(pDescarga);
            c.setProMontacarga(pMontacarga);
            c.setProDesconsolidacion(pDesconsol);
            c.setProVistosBuenos(pVistos);
            c.setProTransmision(pTransm);
            c.setProComisionAgencia(pAgencia);
            c.setProVobo(pVobo);
            c.setProGastosOperativos(pGastosOp);
            c.setProResguardo(pResguardo);
            c.setProIgv(pIgv);
            c.setProIpm(pIpm);
            c.setProPercepcion(pPercep);
            c.setProOtros1(pOtros1);
            c.setProOtros2(pOtros2);
            c.setProOtros3(pOtros3);
            c.setProOtros4(pOtros4);

            c.setProCargaDescarga(pDescarga.add(pMontacarga));
            c.setProGastosAduaneros(pVistos.add(pTransm).add(pAgencia).add(pVobo).add(pGastosOp));
            c.setProSeguroResguardo(pResguardo);
            c.setProImpuestos(pIgv.add(pIpm).add(pPercep));
            c.setProOtrosGastos(pOtros1.add(pOtros2).add(pOtros3).add(pOtros4));

            BigDecimal costoLandedFactura = baseValor
                    .add(pFlete).add(pAlmacen).add(pTransporte).add(pDescarga).add(pMontacarga)
                    .add(pDesconsol)
                    .add(pVistos).add(pTransm).add(pAgencia).add(pVobo).add(pGastosOp)
                    .add(pResguardo)
                    .add(pIgv).add(pIpm).add(pPercep)
                    .add(c.getProAdv())
                    .add(pOtros1).add(pOtros2).add(pOtros3).add(pOtros4);

            c.setCostoTotalImportacion(costoLandedFactura);

            distribuirCostosAItems(c);

            compraRepository.save(c);
        }

        Importacion saved = importacionRepository.save(imp);
        return mapToResponseDTO(saved);
    }

    private void distribuirCostosAItems(Compra c) {
        List<CompraDetalle> detalles = c.getDetalles();
        if (detalles == null || detalles.isEmpty()) return;

        BigDecimal sumaSubtotalesPuros = BigDecimal.ZERO;
        for (CompraDetalle item : detalles) {
            if (item.getPrecioUnitario() != null && item.getCantidad() != null) {
                sumaSubtotalesPuros = sumaSubtotalesPuros.add(item.getPrecioUnitario().multiply(new BigDecimal(item.getCantidad())));
            }
        }
        if (sumaSubtotalesPuros.compareTo(BigDecimal.ZERO) == 0) return;

        BigDecimal totalSobrecostosProrrateables = c.getCostoTotalImportacion()
                .subtract(sumaSubtotalesPuros)
                .subtract(orZero(c.getProAdv()));

        for (CompraDetalle item : detalles) {
            BigDecimal importeFobItem = BigDecimal.ZERO;
            if (item.getPrecioUnitario() != null && item.getCantidad() != null) {
                importeFobItem = item.getPrecioUnitario().multiply(new BigDecimal(item.getCantidad()));
            }

            BigDecimal factor = importeFobItem.divide(sumaSubtotalesPuros, 10, RoundingMode.HALF_UP);
            BigDecimal sobrecostoItem = totalSobrecostosProrrateables.multiply(factor);
            BigDecimal costoTotalLanded = importeFobItem.add(sobrecostoItem).add(orZero(item.getAdValoremItem()));

            BigDecimal costoUnitarioLanded = BigDecimal.ZERO;
            if (item.getCantidad() != null && item.getCantidad() > 0) {
                costoUnitarioLanded = costoTotalLanded.divide(new BigDecimal(item.getCantidad()), 4, RoundingMode.HALF_UP);
            }

            item.setCostoTotalLanded(costoTotalLanded);
            item.setCostoUnitarioLanded(costoUnitarioLanded);
        }
    }

    @Override
    public void recalcularCostos(Integer id) {}

    @Override
    @Transactional
    public void confirmarRecepcion(Integer id, List<RecepcionItemDTO> items) {
        Importacion importacion = importacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Importación no encontrada ID: " + id));

        if (importacion.getEstado() == EstadoImportacion.CERRADO || importacion.getEstado() == EstadoImportacion.LIQUIDADA) {
            throw new RuntimeException("Esta importación ya fue recibida y cerrada.");
        }

        for (RecepcionItemDTO item : items) {
            CompraDetalle detalle = compraDetalleRepository.findById(item.getDetalleId())
                    .orElseThrow(() -> new RuntimeException("Detalle no encontrado ID: " + item.getDetalleId()));

            detalle.setCantidadRecibida(item.getCantidadRecibida());
            compraDetalleRepository.save(detalle);

            Producto producto = detalle.getProducto();
            if (producto != null) {
                int stockActual = producto.getStockActual() != null ? producto.getStockActual() : 0;
                producto.setStockActual(stockActual + item.getCantidadRecibida());
                productoRepository.save(producto);
            }
        }

        importacion.setEstado(EstadoImportacion.CERRADO);
        importacionRepository.save(importacion);
    }

    private BigDecimal prorratear(BigDecimal costoGlobal, BigDecimal baseTotal, BigDecimal baseIndividual) {
        if (costoGlobal == null || costoGlobal.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        if (baseTotal == null || baseTotal.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        if (baseIndividual == null) return BigDecimal.ZERO;

        return costoGlobal.divide(baseTotal, 10, RoundingMode.HALF_UP).multiply(baseIndividual).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal orZero(BigDecimal val) { return val != null ? val : BigDecimal.ZERO; }

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

        dto.setSumaFobTotal(imp.getSumaFobTotal());
        dto.setPesoTotalKg(imp.getPesoTotalKg());
        dto.setCbmTotal(imp.getCbmTotal());

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

            BigDecimal totalValor = imp.getSumaFobTotal();
            BigDecimal totalPeso = imp.getPesoTotalKg();
            BigDecimal totalCbm = imp.getCbmTotal();

            BigDecimal baseValor = c.getTotal();
            BigDecimal basePeso = orZero(c.getPesoNetoKg());
            BigDecimal baseCbm = orZero(c.getCbm());

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

            if (c.getDetalles() != null) {
                BigDecimal sumaPuraDTO = BigDecimal.ZERO;
                for (CompraDetalle d : c.getDetalles()) {
                    if (d.getPrecioUnitario() != null && d.getCantidad() != null) {
                        sumaPuraDTO = sumaPuraDTO.add(d.getPrecioUnitario().multiply(new BigDecimal(d.getCantidad())));
                    }
                }
                final BigDecimal totalSubtotalesPuros = sumaPuraDTO;

                BigDecimal costoExtraFactura = BigDecimal.ZERO;
                if(c.getTotal() != null) {
                    costoExtraFactura = c.getTotal().subtract(totalSubtotalesPuros);
                }
                final BigDecimal diferenciaOrigen = costoExtraFactura;

                List<ImportacionResponseDTO.DetalleItemDTO> itemsDto = c.getDetalles().stream().map(d -> {
                    ImportacionResponseDTO.DetalleItemDTO item = new ImportacionResponseDTO.DetalleItemDTO();
                    item.setId(d.getId());

                    if (d.getProducto() != null) {
                        item.setNombreProducto(d.getProducto().getNombre());
                    } else {
                        item.setNombreProducto(d.getNombreProducto() != null ? d.getNombreProducto() : "Ítem Libre");
                    }

                    item.setCantidad(new BigDecimal(d.getCantidad()));
                    item.setCantidadRecibida(d.getCantidadRecibida());
                    item.setPrecioUnitarioFob(d.getPrecioUnitario());

                    BigDecimal importeFobReal = BigDecimal.ZERO;
                    if (d.getPrecioUnitario() != null && d.getCantidad() != null) {
                        importeFobReal = d.getPrecioUnitario().multiply(new BigDecimal(d.getCantidad()));
                    }
                    item.setImporteFob(importeFobReal);

                    BigDecimal factor = BigDecimal.ZERO;
                    if (totalSubtotalesPuros.compareTo(BigDecimal.ZERO) > 0) {
                        factor = importeFobReal.divide(totalSubtotalesPuros, 10, RoundingMode.HALF_UP);
                    }
                    item.setFactorParticipacion(factor);

                    BigDecimal extraOrigenItem = diferenciaOrigen.multiply(factor);

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
                    item.setItemIgv(orZero(c.getProIgv()).multiply(factor));
                    item.setItemIpm(orZero(c.getProIpm()).multiply(factor));
                    item.setItemPercepcion(orZero(c.getProPercepcion()).multiply(factor));
                    item.setItemAdv(orZero(d.getAdValoremItem()));
                    item.setItemOtros1(orZero(c.getProOtros1()).multiply(factor));
                    item.setItemOtros2(orZero(c.getProOtros2()).multiply(factor).add(extraOrigenItem));

                    BigDecimal totalLandedCalculado = importeFobReal
                            .add(item.getItemFlete()).add(item.getItemAlmacenaje()).add(item.getItemTransporte())
                            .add(item.getItemDescarga()).add(item.getItemMontacarga()).add(item.getItemDesconsolidacion())
                            .add(item.getItemVistosBuenos()).add(item.getItemTransmision()).add(item.getItemAgente())
                            .add(item.getItemVobo()).add(item.getItemGastosOp()).add(item.getItemResguardo())
                            .add(item.getItemIgv()).add(item.getItemIpm()).add(item.getItemPercepcion())
                            .add(item.getItemAdv()).add(item.getItemOtros1()).add(item.getItemOtros2());

                    item.setCostoTotalLanded(d.getCostoTotalLanded() != null ? d.getCostoTotalLanded() : totalLandedCalculado);

                    BigDecimal unitarioLandedCalculado = BigDecimal.ZERO;
                    if (d.getCantidad() != null && d.getCantidad() > 0) {
                        unitarioLandedCalculado = totalLandedCalculado.divide(new BigDecimal(d.getCantidad()), 4, RoundingMode.HALF_UP);
                    }
                    item.setCostoUnitarioLanded(d.getCostoUnitarioLanded() != null ? d.getCostoUnitarioLanded() : unitarioLandedCalculado);

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