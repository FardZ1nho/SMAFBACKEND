package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.DetalleVentaRequestDTO;
import com.upc.smaf.dtos.request.VentaRequestDTO;
import com.upc.smaf.dtos.response.DetalleVentaResponseDTO;
import com.upc.smaf.dtos.response.VentaResponseDTO;
import com.upc.smaf.entities.*;
import com.upc.smaf.entities.EstadoVenta;
import com.upc.smaf.repositories.ProductoRepository;
import com.upc.smaf.repositories.VentaRepository;
import com.upc.smaf.serviceinterface.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;

    private static final BigDecimal IGV_PORCENTAJE = new BigDecimal("0.18"); // 18%

    // ========== CREAR VENTA COMPLETA ==========

    @Override
    @Transactional
    public VentaResponseDTO crearVenta(VentaRequestDTO request) {
        // 1. Validar que hay productos
        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            throw new RuntimeException("Debe agregar al menos un producto a la venta");
        }

        // 2. Crear la venta
        Venta venta = new Venta();
        venta.setCodigo(generarCodigoVenta());
        venta.setFechaVenta(request.getFechaVenta() != null ? request.getFechaVenta() : LocalDateTime.now());
        venta.setNombreCliente(request.getNombreCliente());
        venta.setTipoCliente(request.getTipoCliente());
        venta.setMetodoPago(request.getMetodoPago());
        venta.setNotas(request.getNotas());
        venta.setEstado(EstadoVenta.COMPLETADA);

        // 3. Procesar cada producto
        for (DetalleVentaRequestDTO detalleDTO : request.getDetalles()) {
            procesarDetalleVenta(venta, detalleDTO, true);
        }

        // 4. Calcular totales
        calcularTotales(venta);

        // 5. Guardar
        Venta ventaGuardada = ventaRepository.save(venta);

        return convertirAResponseDTO(ventaGuardada);
    }

    // ========== GUARDAR BORRADOR ==========

    @Override
    @Transactional
    public VentaResponseDTO guardarBorrador(VentaRequestDTO request) {
        // 1. Crear la venta en estado BORRADOR
        Venta venta = new Venta();
        venta.setCodigo(generarCodigoVenta());
        venta.setFechaVenta(request.getFechaVenta() != null ? request.getFechaVenta() : LocalDateTime.now());
        venta.setNombreCliente(request.getNombreCliente());
        venta.setTipoCliente(request.getTipoCliente());
        venta.setMetodoPago(request.getMetodoPago());
        venta.setNotas(request.getNotas());
        venta.setEstado(EstadoVenta.BORRADOR);

        // 2. Agregar detalles SIN descontar stock
        if (request.getDetalles() != null && !request.getDetalles().isEmpty()) {
            for (DetalleVentaRequestDTO detalleDTO : request.getDetalles()) {
                procesarDetalleVenta(venta, detalleDTO, false); // false = no descontar stock
            }
        }

        // 3. Calcular totales
        calcularTotales(venta);

        // 4. Guardar
        Venta ventaGuardada = ventaRepository.save(venta);

        return convertirAResponseDTO(ventaGuardada);
    }

    // ========== CONVERTIR BORRADOR A VENTA ==========

    @Override
    @Transactional
    public VentaResponseDTO completarVenta(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));

        if (venta.getEstado() != EstadoVenta.BORRADOR) {
            throw new RuntimeException("Solo se pueden completar ventas en estado BORRADOR");
        }

        // Descontar stock de todos los productos
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();

            if (producto.getStockActual() < detalle.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre() +
                        ". Disponible: " + producto.getStockActual() + ", Requerido: " + detalle.getCantidad());
            }

            producto.setStockActual(producto.getStockActual() - detalle.getCantidad());
            productoRepository.save(producto);
        }

        // Cambiar estado a COMPLETADA
        venta.setEstado(EstadoVenta.COMPLETADA);
        Venta ventaActualizada = ventaRepository.save(venta);

        return convertirAResponseDTO(ventaActualizada);
    }

    // ========== CANCELAR VENTA ==========

    @Override
    @Transactional
    public void cancelarVenta(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));

        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            throw new RuntimeException("La venta ya está cancelada");
        }

        // Si la venta estaba COMPLETADA, devolver el stock
        if (venta.getEstado() == EstadoVenta.COMPLETADA) {
            for (DetalleVenta detalle : venta.getDetalles()) {
                Producto producto = detalle.getProducto();
                producto.setStockActual(producto.getStockActual() + detalle.getCantidad());
                productoRepository.save(producto);
            }
        }

        venta.setEstado(EstadoVenta.CANCELADA);
        ventaRepository.save(venta);
    }

    // ========== OBTENER VENTA ==========

    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO obtenerVenta(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
        return convertirAResponseDTO(venta);
    }

    // ========== BUSCAR POR CÓDIGO ==========

    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO buscarPorCodigo(String codigo) {
        Venta venta = ventaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con código: " + codigo));
        return convertirAResponseDTO(venta);
    }

    // ========== LISTAR ==========

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listarTodas() {
        return ventaRepository.findAll().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listarPorEstado(EstadoVenta estado) {
        return ventaRepository.findByEstado(estado).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listarBorradores() {
        return listarPorEstado(EstadoVenta.BORRADOR);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listarCompletadas() {
        return listarPorEstado(EstadoVenta.COMPLETADA);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listarPorFecha(LocalDateTime inicio, LocalDateTime fin) {
        return ventaRepository.findByFechaVentaBetween(inicio, fin).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> buscarPorCliente(String nombreCliente) {
        return ventaRepository.buscarPorCliente(nombreCliente).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
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

        // Actualizar campos básicos
        venta.setNombreCliente(request.getNombreCliente());
        venta.setTipoCliente(request.getTipoCliente());
        venta.setMetodoPago(request.getMetodoPago());
        venta.setNotas(request.getNotas());

        // Limpiar detalles anteriores
        venta.getDetalles().clear();

        // Agregar nuevos detalles
        if (request.getDetalles() != null) {
            for (DetalleVentaRequestDTO detalleDTO : request.getDetalles()) {
                procesarDetalleVenta(venta, detalleDTO, false);
            }
        }

        // Recalcular totales
        calcularTotales(venta);

        Venta ventaActualizada = ventaRepository.save(venta);
        return convertirAResponseDTO(ventaActualizada);
    }

    // ========== ELIMINAR VENTA ==========

    @Override
    @Transactional
    public void eliminarVenta(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));

        if (venta.getEstado() == EstadoVenta.COMPLETADA) {
            throw new RuntimeException("No se puede eliminar una venta COMPLETADA. Debe cancelarla primero.");
        }

        ventaRepository.delete(venta);
    }

    // ========== ESTADÍSTICAS ==========

    @Override
    @Transactional(readOnly = true)
    public Long contarVentasPorEstado(EstadoVenta estado) {
        return ventaRepository.contarPorEstado(estado);
    }

    @Override
    @Transactional
    public VentaResponseDTO convertirBorradorAVenta(Integer id) {
        return completarVenta(id);
    }

    // ========== MÉTODOS AUXILIARES ==========

    private void procesarDetalleVenta(Venta venta, DetalleVentaRequestDTO detalleDTO, boolean descontarStock) {
        Producto producto = productoRepository.findById(detalleDTO.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + detalleDTO.getProductoId()));

        // Validar stock solo si se va a descontar
        if (descontarStock && producto.getStockActual() < detalleDTO.getCantidad()) {
            throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre() +
                    ". Disponible: " + producto.getStockActual() + ", Solicitado: " + detalleDTO.getCantidad());
        }

        // Crear detalle
        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(detalleDTO.getCantidad());
        detalle.setPrecioUnitario(detalleDTO.getPrecioUnitario());
        detalle.setDescuento(detalleDTO.getDescuento() != null ? detalleDTO.getDescuento() : BigDecimal.ZERO);
        detalle.calcularSubtotal();

        venta.agregarDetalle(detalle);

        // Descontar stock si es necesario
        if (descontarStock) {
            producto.setStockActual(producto.getStockActual() - detalleDTO.getCantidad());
            productoRepository.save(producto);
        }
    }

    private void calcularTotales(Venta venta) {
        // Calcular subtotal sumando todos los detalles
        BigDecimal subtotal = venta.getDetalles().stream()
                .map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcular IGV (18%)
        BigDecimal igv = subtotal.multiply(IGV_PORCENTAJE).setScale(2, RoundingMode.HALF_UP);

        // Calcular total
        BigDecimal total = subtotal.add(igv);

        venta.setSubtotal(subtotal);
        venta.setIgv(igv);
        venta.setTotal(total);
    }

    private String generarCodigoVenta() {
        // Formato: VTA-2025-0001
        String anio = String.valueOf(LocalDateTime.now().getYear());
        Long contador = ventaRepository.count() + 1;
        return String.format("VTA-%s-%04d", anio, contador);
    }

    private VentaResponseDTO convertirAResponseDTO(Venta venta) {
        VentaResponseDTO dto = new VentaResponseDTO();
        dto.setId(venta.getId());
        dto.setCodigo(venta.getCodigo());
        dto.setFechaVenta(venta.getFechaVenta());
        dto.setNombreCliente(venta.getNombreCliente());
        dto.setTipoCliente(venta.getTipoCliente());
        dto.setMetodoPago(venta.getMetodoPago());
        dto.setSubtotal(venta.getSubtotal());
        dto.setIgv(venta.getIgv());
        dto.setTotal(venta.getTotal());
        dto.setNotas(venta.getNotas());
        dto.setEstado(venta.getEstado());
        dto.setFechaCreacion(venta.getFechaCreacion());

        // Convertir detalles
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