package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.ProductoRequestDTO;
import com.upc.smaf.dtos.response.ProductoResponseDTO;
import com.upc.smaf.entities.Categoria;
import com.upc.smaf.entities.Producto;
import com.upc.smaf.repositories.CategoriaRepository;
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

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional
    public ProductoResponseDTO crearProducto(ProductoRequestDTO request) {
        // 1. Validar que la categoría existe
        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // 2. Validar que el código no exista (solo entre productos ACTIVOS)
        if (request.getCodigo() != null &&
                productoRepository.findByCodigoAndActivoTrue(request.getCodigo()).isPresent()) {
            throw new RuntimeException("Ya existe un producto activo con ese código");
        }

        // 3. Generar código si no se proporciona
        String codigo = request.getCodigo();
        if (codigo == null || codigo.trim().isEmpty()) {
            codigo = generarCodigoProducto(request.getNombre());
        }

        // 4. Crear producto
        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setCodigo(codigo);
        producto.setDescripcion(request.getDescripcion());
        producto.setCategoria(categoria);
        producto.setStockActual(request.getStockActual());
        producto.setStockMinimo(request.getStockMinimo());

        // ⭐⭐⭐ TRES PRECIOS ⭐⭐⭐
        producto.setPrecioChina(request.getPrecioChina());
        producto.setCostoTotal(request.getCostoTotal());
        producto.setPrecioVenta(request.getPrecioVenta());

        // ⭐⭐⭐ MONEDA ⭐⭐⭐
        producto.setMoneda(request.getMoneda() != null ? request.getMoneda() : "USD");
        producto.setUnidadMedida(request.getUnidadMedida() != null ? request.getUnidadMedida() : "unidad");

        producto.setActivo(true);

        producto = productoRepository.save(producto);
        return convertirAResponseDTO(producto);
    }

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

        // Validar que la categoría existe
        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // Validar código único (si cambia) - solo entre productos ACTIVOS
        if (request.getCodigo() != null &&
                !request.getCodigo().equals(producto.getCodigo()) &&
                productoRepository.findByCodigoAndActivoTrue(request.getCodigo()).isPresent()) {
            throw new RuntimeException("Ya existe un producto activo con ese código");
        }

        // Actualizar campos
        producto.setNombre(request.getNombre());
        producto.setCodigo(request.getCodigo());
        producto.setDescripcion(request.getDescripcion());
        producto.setCategoria(categoria);
        producto.setStockActual(request.getStockActual());
        producto.setStockMinimo(request.getStockMinimo());

        // ⭐⭐⭐ ACTUALIZAR TRES PRECIOS ⭐⭐⭐
        producto.setPrecioChina(request.getPrecioChina());
        producto.setCostoTotal(request.getCostoTotal());
        producto.setPrecioVenta(request.getPrecioVenta());

        // ⭐⭐⭐ ACTUALIZAR MONEDA ⭐⭐⭐
        if (request.getMoneda() != null) {
            producto.setMoneda(request.getMoneda());
        }
        if (request.getUnidadMedida() != null) {
            producto.setUnidadMedida(request.getUnidadMedida());
        }

        producto = productoRepository.save(producto);
        return convertirAResponseDTO(producto);
    }

    @Override
    @Transactional
    public void desactivarProducto(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Eliminación lógica: Solo cambiar el estado a false
        producto.setActivo(false);
        productoRepository.save(producto);

        System.out.println("✅ Producto desactivado (eliminación lógica): " + producto.getCodigo());
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

        if (producto.getStockActual() <= 0) {
            return "AGOTADO";
        } else if (producto.getStockActual() < producto.getStockMinimo()) {
            return "BAJO";
        } else if (producto.getStockActual() < producto.getStockMinimo() * 2) {
            return "NORMAL";
        } else {
            return "ALTO";
        }
    }

    // ========== MÉTODOS PRIVADOS AUXILIARES ==========

    private String generarCodigoProducto(String nombre) {
        // Simple: primeras 3 letras + timestamp
        String prefijo = nombre.length() >= 3 ?
                nombre.substring(0, 3).toUpperCase() :
                nombre.toUpperCase();
        return prefijo + "-" + System.currentTimeMillis() % 10000;
    }

    private ProductoResponseDTO convertirAResponseDTO(Producto producto) {
        ProductoResponseDTO response = new ProductoResponseDTO();
        response.setId(producto.getId());
        response.setNombre(producto.getNombre());
        response.setCodigo(producto.getCodigo());
        response.setDescripcion(producto.getDescripcion());

        // Información de categoría
        if (producto.getCategoria() != null) {
            response.setIdCategoria(producto.getCategoria().getId());
            response.setNombreCategoria(producto.getCategoria().getNombre());
        }

        response.setStockActual(producto.getStockActual());
        response.setStockMinimo(producto.getStockMinimo());

        // ⭐⭐⭐ TRES PRECIOS ⭐⭐⭐
        response.setPrecioChina(producto.getPrecioChina());
        response.setCostoTotal(producto.getCostoTotal());
        response.setPrecioVenta(producto.getPrecioVenta());

        // ⭐⭐⭐ MONEDA Y UNIDAD ⭐⭐⭐
        response.setMoneda(producto.getMoneda());
        response.setUnidadMedida(producto.getUnidadMedida());

        response.setActivo(producto.getActivo());
        response.setFechaCreacion(producto.getFechaCreacion());

        // ⭐⭐⭐ CALCULAR MARGEN DE GANANCIA ⭐⭐⭐
        calcularMargenGanancia(response);

        // Calcular estado de stock
        calcularEstadoStock(response);

        return response;
    }

    private void calcularMargenGanancia(ProductoResponseDTO producto) {
        if (producto.getPrecioVenta() != null && producto.getCostoTotal() != null) {
            // Margen = Precio Venta - Costo Total
            BigDecimal margen = producto.getPrecioVenta().subtract(producto.getCostoTotal());
            producto.setMargenGanancia(margen);

            // Porcentaje de margen = (Margen / Precio Venta) * 100
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