package com.upc.smaf.servicesimplements;

import com.upc.smaf.dtos.request.ProductoAlmacenRequestDTO;
import com.upc.smaf.dtos.request.ProductoRequestDTO;
import com.upc.smaf.dtos.response.ProductoResponseDTO;
import com.upc.smaf.entities.*;
import com.upc.smaf.repositories.*;
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
    private final AlmacenRepository almacenRepository;
    private final ProductoAlmacenRepository productoAlmacenRepository;
    private final CompraDetalleRepository compraDetalleRepository;

    // ==========================================
    // 1. CREAR PRODUCTO
    // ==========================================
    @Override
    @Transactional
    public ProductoResponseDTO crearProducto(ProductoRequestDTO request) {
        if (request.getCodigo() != null && !request.getCodigo().isEmpty()) {
            if (productoRepository.existsByCodigo(request.getCodigo())) {
                throw new RuntimeException("El código SKU ya existe");
            }
        }

        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setCodigo(request.getCodigo());
        producto.setCodigoInternacional(request.getCodigoInternacional());
        producto.setDescripcion(request.getDescripcion());

        if (request.getTipo() != null) {
            try {
                producto.setTipo(TipoProducto.valueOf(request.getTipo()));
            } catch (IllegalArgumentException e) {
                producto.setTipo(TipoProducto.PRODUCTO);
            }
        } else {
            producto.setTipo(TipoProducto.PRODUCTO);
        }

        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        producto.setCategoria(categoria);

        producto.setStockMinimo(request.getStockMinimo());
        producto.setPrecioVenta(request.getPrecioVenta());
        producto.setUnidadMedida(request.getUnidadMedida());
        producto.setPrecioChina(request.getPrecioChina());
        producto.setCostoTotal(request.getCostoTotal());
        producto.setMoneda(request.getMoneda());

        if (producto.getStockActual() == null) {
            producto.setStockActual(0);
        }

        if (producto.getTipo() == TipoProducto.KIT && request.getComponentes() != null) {
            for (ProductoRequestDTO.ComponenteDTO compDto : request.getComponentes()) {
                Producto hijo = productoRepository.findById(compDto.getIdProducto())
                        .orElseThrow(() -> new RuntimeException("Componente no encontrado ID: " + compDto.getIdProducto()));

                ProductoKit pk = new ProductoKit();
                pk.setKit(producto);
                pk.setComponente(hijo);
                pk.setCantidad(compDto.getCantidad());

                producto.getComponentes().add(pk);
            }
        }

        Producto guardado = productoRepository.save(producto);
        return convertirAResponseDTO(guardado);
    }

    // ==========================================
    // 2. AGREGAR STOCK (Solo Productos Físicos)
    // ==========================================
    @Override
    @Transactional
    public ProductoAlmacen agregarStock(ProductoAlmacenRequestDTO dto) {
        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (producto.getTipo() == TipoProducto.KIT) {
            throw new RuntimeException("No se puede agregar stock físico a un KIT. Agregue stock a sus componentes individuales.");
        }

        // Opcional: Bloquear agregar stock a servicios si quieres ser estricto
        // if (producto.getTipo() == TipoProducto.SERVICIO) return null;

        Almacen almacen = almacenRepository.findById(dto.getAlmacenId())
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado"));

        ProductoAlmacen pa = productoAlmacenRepository.findByProductoAndAlmacen(producto, almacen)
                .orElseGet(() -> {
                    ProductoAlmacen nuevo = new ProductoAlmacen();
                    nuevo.setProducto(producto);
                    nuevo.setAlmacen(almacen);
                    nuevo.setStock(0);
                    nuevo.setActivo(true);
                    return nuevo;
                });

        pa.setStock(pa.getStock() + dto.getCantidad());

        if (dto.getUbicacionFisica() != null && !dto.getUbicacionFisica().isBlank()) {
            pa.setUbicacionFisica(dto.getUbicacionFisica());
        }
        if (dto.getStockMinimo() != null) {
            pa.setStockMinimo(dto.getStockMinimo());
        }

        productoAlmacenRepository.save(pa);

        producto.calcularStockTotal();
        productoRepository.save(producto);

        return pa;
    }

    // ==========================================
    // 3. ACTUALIZAR PRODUCTO
    // ==========================================
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
            throw new RuntimeException("Ya existe un producto activo con ese código SKU");
        }

        producto.setNombre(request.getNombre());
        if (request.getCodigo() != null) producto.setCodigo(request.getCodigo());
        producto.setCodigoInternacional(request.getCodigoInternacional());
        producto.setDescripcion(request.getDescripcion());
        producto.setCategoria(categoria);

        if (request.getStockMinimo() != null) producto.setStockMinimo(request.getStockMinimo());

        producto.setPrecioChina(request.getPrecioChina());
        producto.setCostoTotal(request.getCostoTotal());
        producto.setPrecioVenta(request.getPrecioVenta());
        if (request.getMoneda() != null) producto.setMoneda(request.getMoneda());
        if (request.getUnidadMedida() != null) producto.setUnidadMedida(request.getUnidadMedida());

        if (producto.getTipo() == TipoProducto.KIT) {
            producto.getComponentes().clear();

            if (request.getComponentes() != null) {
                for (ProductoRequestDTO.ComponenteDTO compDto : request.getComponentes()) {
                    Producto hijo = productoRepository.findById(compDto.getIdProducto())
                            .orElseThrow(() -> new RuntimeException("Componente no encontrado ID: " + compDto.getIdProducto()));

                    ProductoKit pk = new ProductoKit();
                    pk.setKit(producto);
                    pk.setComponente(hijo);
                    pk.setCantidad(compDto.getCantidad());
                    producto.getComponentes().add(pk);
                }
            }
        }

        producto = productoRepository.save(producto);
        return convertirAResponseDTO(producto);
    }

    // ==========================================
    // 4. REDUCIR STOCK (Ventas) - ✅ AQUÍ ESTÁ EL CAMBIO IMPORTANTE
    // ==========================================
    @Override
    @Transactional
    public void reducirStock(Integer idProducto, int cantidadVenta) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // ✅ CORRECCIÓN CLAVE: Si es SERVICIO, ignoramos el stock y salimos
        if (producto.getTipo() == TipoProducto.SERVICIO) {
            return;
        }

        // CASO A: ES UN KIT (Virtual)
        if (producto.getTipo() == TipoProducto.KIT) {
            // Recorremos su receta y descontamos a cada hijo
            for (ProductoKit componenteKit : producto.getComponentes()) {
                Producto hijo = componenteKit.getComponente();
                int cantidadNecesaria = componenteKit.getCantidad() * cantidadVenta;

                // Recursividad: Descontamos a los hijos
                reducirStock(hijo.getId(), cantidadNecesaria);
            }
            return;
        }

        // CASO B: ES UN PRODUCTO FÍSICO (Stock real)
        if (producto.getStockActual() < cantidadVenta) {
            throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
        }

        // Descontar de almacenes específicos si existen
        if (!producto.getProductosAlmacen().isEmpty()) {
            int cantidadRestante = cantidadVenta;

            for (ProductoAlmacen pa : producto.getProductosAlmacen()) {
                if (cantidadRestante <= 0) break;
                if (!pa.getActivo() || pa.getStock() <= 0) continue;

                int aDescontar = Math.min(pa.getStock(), cantidadRestante);
                pa.setStock(pa.getStock() - aDescontar);
                cantidadRestante -= aDescontar;
            }

            producto.calcularStockTotal(); // Recalcular total
            productoRepository.save(producto);
        } else {
            // Fallback simple si no usa almacenes
            producto.setStockActual(producto.getStockActual() - cantidadVenta);
            productoRepository.save(producto);
        }
    }

    // ==========================================
    // 5. MÉTODOS DE LECTURA Y UTILITARIOS
    // ==========================================

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
    public void desactivarProducto(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    @Override
    public List<ProductoResponseDTO> obtenerProductosConStockBajo() {
        return productoRepository.findByActivoTrue().stream()
                .map(this::convertirAResponseDTO)
                .filter(dto -> dto.getStockActual() < dto.getStockMinimo())
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

    // ==========================================
    // 6. ESTADOS Y STOCK VIRTUAL
    // ==========================================

    @Override
    public Boolean necesitaReorden(Integer idProducto) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        int stockReal = calcularStockVirtual(producto);
        return stockReal < producto.getStockMinimo();
    }

    @Override
    public String obtenerEstadoStock(Integer idProducto) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        int stockReal = calcularStockVirtual(producto);
        int minimo = producto.getStockMinimo();

        if (stockReal <= 0) return "AGOTADO";
        else if (stockReal < minimo) return "BAJO";
        else if (stockReal < minimo * 2) return "NORMAL";
        else return "ALTO";
    }

    private int calcularStockVirtual(Producto producto) {
        // Si es SERVICIO, retornamos un número alto para que parezca siempre disponible
        if (producto.getTipo() == TipoProducto.SERVICIO) {
            return 9999;
        }

        if (producto.getTipo() != TipoProducto.KIT) {
            return producto.getStockActual() != null ? producto.getStockActual() : 0;
        }

        if (producto.getComponentes() == null || producto.getComponentes().isEmpty()) {
            return 0;
        }

        int stockPosible = Integer.MAX_VALUE;

        for (ProductoKit pk : producto.getComponentes()) {
            Producto hijo = pk.getComponente();
            Integer cantidadRequerida = pk.getCantidad();

            if (cantidadRequerida <= 0) continue;

            int stockHijo = calcularStockVirtual(hijo);

            if (stockHijo == 0) return 0;

            int kitsPorComponente = stockHijo / cantidadRequerida;

            if (kitsPorComponente < stockPosible) {
                stockPosible = kitsPorComponente;
            }
        }

        return (stockPosible == Integer.MAX_VALUE) ? 0 : stockPosible;
    }

    // ==========================================
    // 7. CONVERTIDOR DTO
    // ==========================================

    private ProductoResponseDTO convertirAResponseDTO(Producto producto) {
        ProductoResponseDTO response = new ProductoResponseDTO();
        response.setId(producto.getId());
        response.setNombre(producto.getNombre());
        response.setCodigo(producto.getCodigo());
        response.setCodigoInternacional(producto.getCodigoInternacional());

        response.setTipo(producto.getTipo() != null ? producto.getTipo().name() : "PRODUCTO");
        response.setDescripcion(producto.getDescripcion());

        if (producto.getCategoria() != null) {
            response.setIdCategoria(producto.getCategoria().getId());
            response.setNombreCategoria(producto.getCategoria().getNombre());
        }

        int stockReal = calcularStockVirtual(producto);

        // Visualmente para servicios mostramos 0 o infinito, depende tu gusto.
        // Aquí dejo 9999 si es servicio para que no salga "Agotado" en frontend
        if(producto.getTipo() == TipoProducto.SERVICIO) {
            response.setStockActual(9999);
        } else {
            response.setStockActual(stockReal);
        }

        response.setStockMinimo(producto.getStockMinimo());

        if (producto.getTipo() == TipoProducto.PRODUCTO) {
            Integer porLlegar = compraDetalleRepository.obtenerStockPorLlegar(producto.getId());
            response.setStockPorLlegar(porLlegar != null ? porLlegar : 0);
        } else {
            response.setStockPorLlegar(0);
        }

        response.setPrecioChina(producto.getPrecioChina());
        response.setCostoTotal(producto.getCostoTotal());
        response.setPrecioVenta(producto.getPrecioVenta());
        response.setMoneda(producto.getMoneda());
        response.setUnidadMedida(producto.getUnidadMedida());
        response.setActivo(producto.getActivo());
        response.setFechaCreacion(producto.getFechaCreacion());

        if (producto.getTipo() == TipoProducto.KIT && producto.getComponentes() != null) {
            List<ProductoResponseDTO.ComponenteResponseDTO> comps = producto.getComponentes().stream().map(pk -> {
                ProductoResponseDTO.ComponenteResponseDTO dto = new ProductoResponseDTO.ComponenteResponseDTO();
                dto.setIdProducto(pk.getComponente().getId());
                dto.setNombre(pk.getComponente().getNombre());
                dto.setCantidad(pk.getCantidad());
                return dto;
            }).collect(Collectors.toList());
            response.setComponentes(comps);
        }

        calcularMargenGanancia(response);
        calcularEstadoStockVisual(response, stockReal);

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

    private void calcularEstadoStockVisual(ProductoResponseDTO response, int stockReal) {
        // Si es servicio, siempre es ALTO/DISPONIBLE
        if ("SERVICIO".equals(response.getTipo())) {
            response.setEstadoStock("ALTO");
            response.setNecesitaReorden(false);
            return;
        }

        int minimo = response.getStockMinimo() != null ? response.getStockMinimo() : 0;

        if (stockReal <= 0) {
            response.setEstadoStock("AGOTADO");
            response.setNecesitaReorden(true);
        } else if (stockReal < minimo) {
            response.setEstadoStock("BAJO");
            response.setNecesitaReorden(true);
        } else if (stockReal < minimo * 2) {
            response.setEstadoStock("NORMAL");
            response.setNecesitaReorden(false);
        } else {
            response.setEstadoStock("ALTO");
            response.setNecesitaReorden(false);
        }
    }
}