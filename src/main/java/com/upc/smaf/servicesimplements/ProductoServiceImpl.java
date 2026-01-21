package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.ProductoAlmacenRequestDTO; // 👈 Importante
import com.upc.smaf.dtos.request.ProductoRequestDTO;
import com.upc.smaf.dtos.response.ProductoResponseDTO;
import com.upc.smaf.entities.*;
import com.upc.smaf.repositories.AlmacenRepository; // 👈 Importante
import com.upc.smaf.repositories.CategoriaRepository;
import com.upc.smaf.repositories.ProductoAlmacenRepository; // 👈 Importante
import com.upc.smaf.repositories.ProductoRepository;
import com.upc.smaf.serviceinterface.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    // ========== 1. INYECCIÓN DE DEPENDENCIAS (Repositorios) ==========
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    // 👇 AGREGAMOS ESTOS DOS QUE FALTABAN PARA LA LÓGICA DE STOCK
    private final AlmacenRepository almacenRepository;
    private final ProductoAlmacenRepository productoAlmacenRepository;

    // ========== 2. IMPLEMENTACIÓN DE MÉTODOS ==========

    @Override
    @Transactional
    public ProductoResponseDTO crearProducto(ProductoRequestDTO request) {
        // Validar código único, categoría, etc... (lo que ya tengas)
        if (productoRepository.existsByCodigo(request.getCodigo())) {
            throw new RuntimeException("El código SKU ya existe");
        }

        Producto producto = new Producto();
        producto.setNombre(request.getNombre());

        // 👇👇 AGREGA ESTO: MAPEO DEL TIPO 👇👇
        if (request.getTipo() != null) {
            try {
                // Convertimos el String "SERVICIO" a Enum
                producto.setTipo(TipoProducto.valueOf(request.getTipo()));
            } catch (IllegalArgumentException e) {
                producto.setTipo(TipoProducto.PRODUCTO); // Fallback
            }
        } else {
            producto.setTipo(TipoProducto.PRODUCTO);
        }
        // 👆👆 FIN DEL BLOQUE NUEVO 👆👆

        producto.setCodigo(request.getCodigo());
        producto.setDescripcion(request.getDescripcion());

        // ... resto de tus setters (Categoría, Precios, etc) ...
        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        producto.setCategoria(categoria);

        producto.setStockMinimo(request.getStockMinimo());
        producto.setPrecioVenta(request.getPrecioVenta());
        producto.setUnidadMedida(request.getUnidadMedida());
        // ... etc ...

        // Si es SERVICIO, forzamos stock 0 y activo true (opcional)
        if (producto.getTipo() == TipoProducto.SERVICIO) {
            producto.setStockActual(0);
        }

        Producto guardado = productoRepository.save(producto);
        return convertirAResponseDTO(guardado);
    }

    // 👇👇👇 3. AQUÍ ESTÁ EL MÉTODO QUE TE FALTABA 👇👇👇
    @Override
    @Transactional
    public ProductoAlmacen agregarStock(ProductoAlmacenRequestDTO dto) {
        // 1. Validar existencia
        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Almacen almacen = almacenRepository.findById(dto.getAlmacenId())
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado"));

        // 2. Buscar o Crear relación
        ProductoAlmacen pa = productoAlmacenRepository.findByProductoAndAlmacen(producto, almacen)
                .orElseGet(() -> {
                    ProductoAlmacen nuevo = new ProductoAlmacen();
                    nuevo.setProducto(producto);
                    nuevo.setAlmacen(almacen);
                    nuevo.setStock(0);
                    nuevo.setActivo(true);
                    return nuevo;
                });

        // 3. Lógica de negocio: SUMAR STOCK
        pa.setStock(pa.getStock() + dto.getCantidad());

        // Actualizar datos opcionales
        if (dto.getUbicacionFisica() != null && !dto.getUbicacionFisica().isBlank()) {
            pa.setUbicacionFisica(dto.getUbicacionFisica());
        }
        if (dto.getStockMinimo() != null) {
            pa.setStockMinimo(dto.getStockMinimo());
        }

        // 4. Guardar movimiento en almacén
        productoAlmacenRepository.save(pa);

        // 5. RECALCULAR STOCK TOTAL DEL PRODUCTO
        producto.calcularStockTotal(); // Asegúrate que tu Entidad Producto tenga este método helper
        productoRepository.save(producto);

        return pa;
    }
    // 👆👆👆 FIN DEL MÉTODO NUEVO 👆👆👆

    @Override
    public ProductoResponseDTO obtenerProducto(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        return convertirAResponseDTO(producto);
    }

    @Override
    public List<ProductoResponseDTO> listarProductos() {
        return productoRepository.findAll().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoResponseDTO> listarProductosActivos() {
        return productoRepository.findByActivoTrue().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductoResponseDTO actualizarProducto(Integer id, ProductoRequestDTO request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        if (request.getCodigo() != null && !request.getCodigo().trim().isEmpty() &&
                !request.getCodigo().equals(producto.getCodigo()) &&
                productoRepository.findByCodigoAndActivoTrue(request.getCodigo()).isPresent()) {
            throw new RuntimeException("Ya existe un producto activo con ese código");
        }

        producto.setNombre(request.getNombre());
        if (request.getCodigo() != null) producto.setCodigo(request.getCodigo());
        producto.setDescripcion(request.getDescripcion());
        producto.setCategoria(categoria);

        if (request.getStockMinimo() != null) producto.setStockMinimo(request.getStockMinimo());

        producto.setPrecioChina(request.getPrecioChina());
        producto.setCostoTotal(request.getCostoTotal());
        producto.setPrecioVenta(request.getPrecioVenta());
        if (request.getMoneda() != null) producto.setMoneda(request.getMoneda());
        if (request.getUnidadMedida() != null) producto.setUnidadMedida(request.getUnidadMedida());

        producto = productoRepository.save(producto);
        return convertirAResponseDTO(producto);
    }

    @Override
    @Transactional
    public void desactivarProducto(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    @Override
    public List<ProductoResponseDTO> obtenerProductosConStockBajo() {
        return productoRepository.findByStockActualLessThanStockMinimoAndActivoTrue().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductoResponseDTO obtenerProductoPorCodigo(String codigo) {
        Producto producto = productoRepository.findByCodigoAndActivoTrue(codigo)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        return convertirAResponseDTO(producto);
    }

    @Override
    public List<ProductoResponseDTO> buscarProductosPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean necesitaReorden(Integer idProducto) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        return producto.getStockActual() < producto.getStockMinimo();
    }

    @Override
    public String obtenerEstadoStock(Integer idProducto) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (producto.getStockActual() <= 0) return "AGOTADO";
        else if (producto.getStockActual() < producto.getStockMinimo()) return "BAJO";
        else if (producto.getStockActual() < producto.getStockMinimo() * 2) return "NORMAL";
        else return "ALTO";
    }

    // ========== MÉTODOS PRIVADOS AUXILIARES ==========

    private String generarCodigoProducto(String nombre) {
        String prefijo = nombre.length() >= 3 ? nombre.substring(0, 3).toUpperCase() : nombre.toUpperCase();
        return prefijo + "-" + System.currentTimeMillis() % 10000;
    }

    private ProductoResponseDTO convertirAResponseDTO(Producto producto) {
        ProductoResponseDTO response = new ProductoResponseDTO();
        response.setId(producto.getId());
        response.setNombre(producto.getNombre());
        response.setCodigo(producto.getCodigo());

        // 👇 ESTA ES LA LÍNEA QUE FALTABA PARA ARREGLAR TU TABLA 👇
        // Convierte el Enum (PRODUCTO/SERVICIO) a String para enviarlo al frontend
        response.setTipo(producto.getTipo().name());

        response.setDescripcion(producto.getDescripcion());

        if (producto.getCategoria() != null) {
            response.setIdCategoria(producto.getCategoria().getId());
            response.setNombreCategoria(producto.getCategoria().getNombre());
        }

        response.setStockActual(producto.getStockActual());
        response.setStockMinimo(producto.getStockMinimo());

        response.setPrecioChina(producto.getPrecioChina());
        response.setCostoTotal(producto.getCostoTotal());
        response.setPrecioVenta(producto.getPrecioVenta());

        response.setMoneda(producto.getMoneda());
        response.setUnidadMedida(producto.getUnidadMedida());

        response.setActivo(producto.getActivo());
        response.setFechaCreacion(producto.getFechaCreacion());

        calcularMargenGanancia(response);
        calcularEstadoStock(response);

        return response;
    }

    private void calcularMargenGanancia(ProductoResponseDTO producto) {
        if (producto.getPrecioVenta() != null && producto.getCostoTotal() != null) {
            BigDecimal margen = producto.getPrecioVenta().subtract(producto.getCostoTotal());
            producto.setMargenGanancia(margen);

            if (producto.getPrecioVenta().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal porcentaje = margen
                        .divide(producto.getPrecioVenta(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                producto.setPorcentajeMargen(porcentaje.doubleValue());
            }
        }
    }

    private void calcularEstadoStock(ProductoResponseDTO producto) {
        if (producto.getStockActual() <= 0) {
            producto.setEstadoStock("AGOTADO");
            producto.setNecesitaReorden(true);
        } else if (producto.getStockActual() < producto.getStockMinimo()) {
            producto.setEstadoStock("BAJO");
            producto.setNecesitaReorden(true);
        } else if (producto.getStockActual() < producto.getStockMinimo() * 2) {
            producto.setEstadoStock("NORMAL");
            producto.setNecesitaReorden(false);
        } else {
            producto.setEstadoStock("ALTO");
            producto.setNecesitaReorden(false);
        }
    }
}