package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.DetalleVentaRequestDTO;
import com.upc.smaf.dtos.request.VentaRequestDTO;
import com.upc.smaf.dtos.response.DetalleVentaResponseDTO;
import com.upc.smaf.dtos.response.VentaResponseDTO;
import com.upc.smaf.entities.*;
// ✅ Importamos el repo de cuentas
import com.upc.smaf.repositories.CuentaBancariaRepository;
import com.upc.smaf.repositories.ProductoRepository;
import com.upc.smaf.repositories.VentaRepository;
import com.upc.smaf.serviceinterface.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    // ✅ 1. INYECCIÓN DEL REPOSITORIO DE CUENTAS
    private final CuentaBancariaRepository cuentaRepository;

    private static final BigDecimal IGV_PORCENTAJE = new BigDecimal("0.18");

    // ========== CREAR VENTA COMPLETA ==========
    @Override
    @Transactional
    public VentaResponseDTO crearVenta(VentaRequestDTO request) {
        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            throw new RuntimeException("Debe agregar al menos un producto a la venta");
        }

        Venta venta = new Venta();
        venta.setCodigo(generarCodigoVenta());
        venta.setFechaVenta(request.getFechaVenta() != null ? request.getFechaVenta() : LocalDateTime.now());

        // Mapear datos básicos
        venta.setNombreCliente(request.getNombreCliente());
        venta.setTipoCliente(request.getTipoCliente());
        venta.setNotas(request.getNotas());

        // Mapear Pagos y Documentos
        mapearDatosPagoYDocumento(venta, request);

        // ✅ 2. GUARDAR LA CUENTA BANCARIA (Si seleccionaron Yape/Plin/Banco)
        if (request.getCuentaBancariaId() != null && request.getCuentaBancariaId() > 0) {
            CuentaBancaria cuenta = cuentaRepository.findById(request.getCuentaBancariaId())
                    .orElseThrow(() -> new RuntimeException("Cuenta bancaria no encontrada"));
            venta.setCuentaBancaria(cuenta);
        }

        // Procesar productos (Descontando stock)
        for (DetalleVentaRequestDTO detalleDTO : request.getDetalles()) {
            procesarDetalleVenta(venta, detalleDTO, true);
        }

        // Calcular Totales
        calcularTotales(venta);

        // Calcular Crédito
        finalizarCalculosCredito(venta);

        // LÓGICA DE ESTADO INICIAL
        if (venta.getTipoPago() == TipoPago.CREDITO && venta.getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0) {
            venta.setEstado(EstadoVenta.PENDIENTE); // Nace con deuda
        } else {
            venta.setEstado(EstadoVenta.COMPLETADA); // Se pagó todo
        }

        Venta ventaGuardada = ventaRepository.save(venta);
        return convertirAResponseDTO(ventaGuardada);
    }

    // ========== REGISTRAR AMORTIZACIÓN (PAGO CUOTA) ==========
    // ✅ 3. AGREGAMOS EL PARÁMETRO cuentaId
    @Override
    @Transactional
    public VentaResponseDTO registrarAmortizacion(Integer ventaId, BigDecimal monto, MetodoPago metodo, Integer cuentaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            throw new RuntimeException("No se puede pagar una venta cancelada");
        }

        if (venta.getSaldoPendiente().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Esta venta ya está pagada por completo");
        }

        if (monto.compareTo(venta.getSaldoPendiente()) > 0) {
            throw new RuntimeException("El monto excede el saldo pendiente (" + venta.getSaldoPendiente() + ")");
        }

        // 1. Crear el Pago
        Pago pago = new Pago();
        pago.setMonto(monto);
        pago.setMetodoPago(metodo);

        // ✅ 4. ASIGNAR CUENTA DESTINO DEL PAGO
        if (cuentaId != null && cuentaId > 0) {
            CuentaBancaria cuenta = cuentaRepository.findById(cuentaId)
                    .orElseThrow(() -> new RuntimeException("Cuenta bancaria no encontrada"));
            pago.setCuentaDestino(cuenta);
        }

        // 2. Agregarlo a la lista usando el helper de la entidad
        venta.agregarPago(pago);

        // 3. Actualizar Saldo Pendiente
        BigDecimal nuevoSaldo = venta.getSaldoPendiente().subtract(monto);
        venta.setSaldoPendiente(nuevoSaldo);

        // 4. Verificar si ya se completó
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) <= 0) {
            venta.setEstado(EstadoVenta.COMPLETADA);
        }

        Venta ventaActualizada = ventaRepository.save(venta);
        return convertirAResponseDTO(ventaActualizada);
    }

    // ========== GUARDAR BORRADOR ==========
    @Override
    @Transactional
    public VentaResponseDTO guardarBorrador(VentaRequestDTO request) {
        Venta venta = new Venta();
        venta.setCodigo(generarCodigoVenta());
        venta.setFechaVenta(request.getFechaVenta() != null ? request.getFechaVenta() : LocalDateTime.now());

        venta.setNombreCliente(request.getNombreCliente());
        venta.setTipoCliente(request.getTipoCliente());
        venta.setNotas(request.getNotas());
        venta.setEstado(EstadoVenta.BORRADOR);

        mapearDatosPagoYDocumento(venta, request);

        // ✅ GUARDAR CUENTA EN BORRADOR
        if (request.getCuentaBancariaId() != null && request.getCuentaBancariaId() > 0) {
            cuentaRepository.findById(request.getCuentaBancariaId()).ifPresent(venta::setCuentaBancaria);
        }

        if (request.getDetalles() != null && !request.getDetalles().isEmpty()) {
            for (DetalleVentaRequestDTO detalleDTO : request.getDetalles()) {
                procesarDetalleVenta(venta, detalleDTO, false);
            }
        }

        calcularTotales(venta);
        finalizarCalculosCredito(venta);

        Venta ventaGuardada = ventaRepository.save(venta);
        return convertirAResponseDTO(ventaGuardada);
    }

    // ========== ACTUALIZAR VENTA ==========
    @Override
    @Transactional
    public VentaResponseDTO actualizarVenta(Integer id, VentaRequestDTO request) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));

        if (venta.getEstado() == EstadoVenta.COMPLETADA) {
            throw new RuntimeException("No se puede actualizar una venta COMPLETADA");
        }

        venta.setNombreCliente(request.getNombreCliente());
        venta.setTipoCliente(request.getTipoCliente());
        venta.setNotas(request.getNotas());
        if(request.getFechaVenta() != null) venta.setFechaVenta(request.getFechaVenta());

        mapearDatosPagoYDocumento(venta, request);

        // ✅ ACTUALIZAR CUENTA
        if (request.getCuentaBancariaId() != null && request.getCuentaBancariaId() > 0) {
            CuentaBancaria cuenta = cuentaRepository.findById(request.getCuentaBancariaId())
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
            venta.setCuentaBancaria(cuenta);
        } else {
            venta.setCuentaBancaria(null); // Si la quitaron
        }

        venta.getDetalles().clear();
        if (request.getDetalles() != null) {
            for (DetalleVentaRequestDTO detalleDTO : request.getDetalles()) {
                procesarDetalleVenta(venta, detalleDTO, false);
            }
        }

        calcularTotales(venta);
        finalizarCalculosCredito(venta);

        Venta ventaActualizada = ventaRepository.save(venta);
        return convertirAResponseDTO(ventaActualizada);
    }

    // ========== COMPLETAR VENTA (Desde Borrador) ==========
    @Override
    @Transactional
    public VentaResponseDTO completarVenta(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));

        if (venta.getEstado() != EstadoVenta.BORRADOR) {
            throw new RuntimeException("Solo se pueden completar ventas en estado BORRADOR");
        }

        // Validar y Descontar stock
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            if (producto.getStockActual() < detalle.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }
            producto.setStockActual(producto.getStockActual() - detalle.getCantidad());
            productoRepository.save(producto);
        }

        // Lógica de Crédito al completar borrador
        if (venta.getTipoPago() == TipoPago.CREDITO && venta.getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0) {
            venta.setEstado(EstadoVenta.PENDIENTE);
        } else {
            venta.setEstado(EstadoVenta.COMPLETADA);
        }

        Venta ventaActualizada = ventaRepository.save(venta);
        return convertirAResponseDTO(ventaActualizada);
    }

    // ========== ELIMINAR ==========
    @Override
    @Transactional
    public void eliminarVenta(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
        if (venta.getEstado() == EstadoVenta.COMPLETADA) {
            throw new RuntimeException("No se puede eliminar una venta COMPLETADA.");
        }
        ventaRepository.delete(venta);
    }

    // ========== CANCELAR ==========
    @Override
    @Transactional
    public void cancelarVenta(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));

        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            throw new RuntimeException("La venta ya está cancelada");
        }

        // Si estaba completada o pendiente (ya descontó stock), devolvemos el stock
        if (venta.getEstado() == EstadoVenta.COMPLETADA || venta.getEstado() == EstadoVenta.PENDIENTE) {
            for (DetalleVenta detalle : venta.getDetalles()) {
                Producto producto = detalle.getProducto();
                producto.setStockActual(producto.getStockActual() + detalle.getCantidad());
                productoRepository.save(producto);
            }
        }

        venta.setEstado(EstadoVenta.CANCELADA);
        ventaRepository.save(venta);
    }

    // ========== LECTURAS ==========
    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO obtenerVenta(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
        return convertirAResponseDTO(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO buscarPorCodigo(String codigo) {
        Venta venta = ventaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con código: " + codigo));
        return convertirAResponseDTO(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listarTodas() {
        return ventaRepository.findAll().stream().map(this::convertirAResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listarPorEstado(EstadoVenta estado) {
        return ventaRepository.findByEstado(estado).stream().map(this::convertirAResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<VentaResponseDTO> listarBorradores() { return listarPorEstado(EstadoVenta.BORRADOR); }
    @Override
    public List<VentaResponseDTO> listarCompletadas() { return listarPorEstado(EstadoVenta.COMPLETADA); }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listarPorFecha(LocalDateTime inicio, LocalDateTime fin) {
        return ventaRepository.findByFechaVentaBetween(inicio, fin).stream().map(this::convertirAResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> buscarPorCliente(String nombreCliente) {
        return ventaRepository.buscarPorCliente(nombreCliente).stream().map(this::convertirAResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarVentasPorEstado(EstadoVenta estado) { return ventaRepository.contarPorEstado(estado); }

    @Override
    @Transactional
    public VentaResponseDTO convertirBorradorAVenta(Integer id) { return completarVenta(id); }

    // ========== MÉTODOS AUXILIARES (HELPERS) ==========

    private void mapearDatosPagoYDocumento(Venta venta, VentaRequestDTO request) {
        venta.setMoneda(request.getMoneda());
        venta.setTipoCambio(request.getTipoCambio());
        venta.setMetodoPago(request.getMetodoPago());
        venta.setTipoPago(request.getTipoPago());
        venta.setTipoDocumento(request.getTipoDocumento());
        venta.setNumeroDocumento(request.getNumeroDocumento());

        if (MetodoPago.MIXTO.equals(request.getMetodoPago())) {
            venta.setPagoEfectivo(request.getPagoEfectivo() != null ? request.getPagoEfectivo() : BigDecimal.ZERO);
            venta.setPagoTransferencia(request.getPagoTransferencia() != null ? request.getPagoTransferencia() : BigDecimal.ZERO);
        } else {
            venta.setPagoEfectivo(BigDecimal.ZERO);
            venta.setPagoTransferencia(BigDecimal.ZERO);
        }

        venta.setMontoInicial(request.getMontoInicial() != null ? request.getMontoInicial() : BigDecimal.ZERO);
        venta.setNumeroCuotas(request.getNumeroCuotas() != null ? request.getNumeroCuotas() : 0);
    }

    private void finalizarCalculosCredito(Venta venta) {
        if (venta.getTipoPago() == TipoPago.CREDITO) {
            if (venta.getMontoInicial().compareTo(venta.getTotal()) > 0) {
                venta.setMontoInicial(venta.getTotal());
            }
            BigDecimal saldo = venta.getTotal().subtract(venta.getMontoInicial());
            venta.setSaldoPendiente(saldo);

            if (saldo.compareTo(BigDecimal.ZERO) > 0 && venta.getNumeroCuotas() > 0) {
                BigDecimal cuota = saldo.divide(new BigDecimal(venta.getNumeroCuotas()), 2, RoundingMode.HALF_UP);
                venta.setMontoCuota(cuota);
            } else {
                venta.setMontoCuota(BigDecimal.ZERO);
            }
        } else {
            venta.setMontoInicial(venta.getTotal());
            venta.setSaldoPendiente(BigDecimal.ZERO);
            venta.setNumeroCuotas(0);
            venta.setMontoCuota(BigDecimal.ZERO);
        }
    }

    private void calcularTotales(Venta venta) {
        BigDecimal subtotal = venta.getDetalles().stream()
                .map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal igv = subtotal.multiply(IGV_PORCENTAJE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv);

        venta.setSubtotal(subtotal);
        venta.setIgv(igv);
        venta.setTotal(total);
    }

    private String generarCodigoVenta() {
        String anio = String.valueOf(LocalDateTime.now().getYear());
        Long contador = ventaRepository.count() + 1;
        return String.format("VTA-%s-%04d", anio, contador);
    }

    private void procesarDetalleVenta(Venta venta, DetalleVentaRequestDTO detalleDTO, boolean descontarStock) {
        Producto producto = productoRepository.findById(detalleDTO.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + detalleDTO.getProductoId()));

        if (descontarStock && producto.getStockActual() < detalleDTO.getCantidad()) {
            throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
        }

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(detalleDTO.getCantidad());
        detalle.setPrecioUnitario(detalleDTO.getPrecioUnitario());
        detalle.setDescuento(detalleDTO.getDescuento() != null ? detalleDTO.getDescuento() : BigDecimal.ZERO);
        detalle.calcularSubtotal();

        venta.agregarDetalle(detalle);

        if (descontarStock) {
            producto.setStockActual(producto.getStockActual() - detalleDTO.getCantidad());
            productoRepository.save(producto);
        }
    }

    private VentaResponseDTO convertirAResponseDTO(Venta venta) {
        VentaResponseDTO dto = new VentaResponseDTO();
        dto.setId(venta.getId());
        dto.setCodigo(venta.getCodigo());
        dto.setFechaVenta(venta.getFechaVenta());
        dto.setNombreCliente(venta.getNombreCliente());
        dto.setTipoCliente(venta.getTipoCliente());
        dto.setEstado(venta.getEstado());

        dto.setTipoPago(venta.getTipoPago());
        dto.setMetodoPago(venta.getMetodoPago());

        // ✅ 5. MAPEAMOS LA CUENTA PARA QUE EL FRONT SEPA A DÓNDE FUE EL DINERO
        if (venta.getCuentaBancaria() != null) {
            dto.setCuentaBancariaId(venta.getCuentaBancaria().getId());
            dto.setNombreCuentaBancaria(venta.getCuentaBancaria().getNombre());
        }

        dto.setPagoEfectivo(venta.getPagoEfectivo());
        dto.setPagoTransferencia(venta.getPagoTransferencia());

        dto.setMontoInicial(venta.getMontoInicial());
        dto.setNumeroCuotas(venta.getNumeroCuotas());
        dto.setMontoCuota(venta.getMontoCuota());
        dto.setSaldoPendiente(venta.getSaldoPendiente());

        dto.setMoneda(venta.getMoneda());
        dto.setTipoCambio(venta.getTipoCambio());
        dto.setTipoDocumento(venta.getTipoDocumento());
        dto.setNumeroDocumento(venta.getNumeroDocumento());

        dto.setSubtotal(venta.getSubtotal());
        dto.setIgv(venta.getIgv());
        dto.setTotal(venta.getTotal());
        dto.setNotas(venta.getNotas());
        dto.setFechaCreacion(venta.getFechaCreacion());
        dto.setFechaActualizacion(venta.getFechaActualizacion());

        List<DetalleVentaResponseDTO> detallesDTO = venta.getDetalles().stream()
                .map(this::convertirDetalleAResponseDTO)
                .collect(Collectors.toList());
        dto.setDetalles(detallesDTO);

        return dto;
    }

    private DetalleVentaResponseDTO convertirDetalleAResponseDTO(DetalleVenta detalle) {
        DetalleVentaResponseDTO dto = new DetalleVentaResponseDTO();
        dto.setId(detalle.getId());
        dto.setProductoId(detalle.getProducto().getId());
        dto.setProductoNombre(detalle.getProducto().getNombre());
        dto.setProductoCodigo(detalle.getProducto().getCodigo());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setDescuento(detalle.getDescuento());
        dto.setSubtotal(detalle.getSubtotal());
        return dto;
    }
}