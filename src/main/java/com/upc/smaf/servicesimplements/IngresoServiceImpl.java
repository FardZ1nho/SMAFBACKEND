package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.IngresoRequestDTO;
import com.upc.smaf.dtos.request.ProductoAlmacenRequestDTO;
import com.upc.smaf.dtos.response.IngresoResponseDTO;
import com.upc.smaf.entities.Almacen;
import com.upc.smaf.entities.Ingreso;
import com.upc.smaf.entities.Producto;
import com.upc.smaf.entities.ProductoAlmacen;
import com.upc.smaf.repositories.AlmacenRepository;
import com.upc.smaf.repositories.IngresoRepository;
import com.upc.smaf.repositories.ProductoAlmacenRepository;
import com.upc.smaf.repositories.ProductoRepository;
import com.upc.smaf.serviceinterface.IngresoService;
import com.upc.smaf.serviceinterface.ProductoAlmacenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngresoServiceImpl implements IngresoService {

    private final IngresoRepository ingresoRepository;
    private final ProductoRepository productoRepository;
    private final AlmacenRepository almacenRepository;
    private final ProductoAlmacenRepository productoAlmacenRepository;
    private final ProductoAlmacenService productoAlmacenService;

    @Override
    @Transactional
    public IngresoResponseDTO registrarIngreso(IngresoRequestDTO request) {
        // 1. Validar que el producto existe
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + request.getProductoId()));

        // 2. Validar que el almacén existe
        Almacen almacen = almacenRepository.findById(request.getAlmacenId())
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado con ID: " + request.getAlmacenId()));

        // 3. Validar que el almacén está activo
        if (!almacen.getActivo()) {
            throw new RuntimeException("El almacén " + almacen.getNombre() + " está inactivo");
        }

        // 4. Crear y guardar el registro de Ingreso (Auditoría)
        Ingreso ingreso = new Ingreso();
        ingreso.setProducto(producto);
        ingreso.setAlmacen(almacen);
        ingreso.setCantidad(request.getCantidad());
        ingreso.setProveedor(request.getProveedor());
        ingreso.setObservacion(request.getObservacion());
        ingreso.setFecha(request.getFecha() != null ? request.getFecha() : LocalDateTime.now());

        ingreso = ingresoRepository.save(ingreso);

        // 5. ACTUALIZAR O CREAR el registro en ProductoAlmacen
        ProductoAlmacen productoAlmacen = productoAlmacenRepository
                .findByProductoIdAndAlmacenId(request.getProductoId(), request.getAlmacenId())
                .orElse(null);

        if (productoAlmacen != null) {
            // Ya existe el producto en ese almacén, incrementar el stock
            productoAlmacenService.ajustarStock(
                    productoAlmacen.getId(),
                    request.getCantidad(),  // Cantidad positiva = incremento
                    "Ingreso de mercadería - " + (request.getObservacion() != null ? request.getObservacion() : "")
            );
        } else {
            // No existe, crear nuevo registro de ProductoAlmacen
            ProductoAlmacenRequestDTO paRequest = new ProductoAlmacenRequestDTO();
            paRequest.setProductoId(request.getProductoId());
            paRequest.setAlmacenId(request.getAlmacenId());
            paRequest.setStock(request.getCantidad());
            paRequest.setActivo(true);

            productoAlmacenService.asignarProductoAAlmacen(paRequest);
        }

        // 6. El stockActual del producto se actualiza automáticamente
        //    dentro de ProductoAlmacenService.ajustarStock() o asignarProductoAAlmacen()

        return convertirAResponseDTO(ingreso);
    }

    @Override
    public List<IngresoResponseDTO> listarHistorialIngresos() {
        return ingresoRepository.findAllByOrderByFechaDesc().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<IngresoResponseDTO> listarIngresosPorProducto(Integer idProducto) {
        // Filtrar ingresos por producto
        return ingresoRepository.findAllByOrderByFechaDesc().stream()
                .filter(ingreso -> ingreso.getProducto().getId().equals(idProducto))
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    // ========== MÉTODO AUXILIAR ==========

    private IngresoResponseDTO convertirAResponseDTO(Ingreso ingreso) {
        IngresoResponseDTO response = new IngresoResponseDTO();
        response.setId(ingreso.getId());
        response.setNombreProducto(ingreso.getProducto().getNombre());
        response.setSkuProducto(ingreso.getProducto().getCodigo());
        response.setCantidad(ingreso.getCantidad());
        response.setFecha(ingreso.getFecha());
        response.setProveedor(ingreso.getProveedor());

        // Información del almacén
        response.setAlmacenId(ingreso.getAlmacen().getId());
        response.setAlmacenCodigo(ingreso.getAlmacen().getCodigo());
        response.setAlmacenNombre(ingreso.getAlmacen().getNombre());

        return response;
    }
}