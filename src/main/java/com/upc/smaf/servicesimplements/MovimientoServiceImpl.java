package com.upc.smaf.servicesimplements;

import com.upc.smaf.entities.Almacen;
import com.upc.smaf.entities.Movimiento;
import com.upc.smaf.entities.Movimiento.TipoMovimiento;
import com.upc.smaf.entities.Producto;
import com.upc.smaf.entities.ProductoAlmacen;
import com.upc.smaf.repositories.AlmacenRepository;
import com.upc.smaf.repositories.MovimientoRepository;
import com.upc.smaf.repositories.ProductoAlmacenRepository;
import com.upc.smaf.repositories.ProductoRepository;
import com.upc.smaf.serviceinterface.MovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final AlmacenRepository almacenRepository;
    private final ProductoAlmacenRepository productoAlmacenRepository;

    @Override
    @Transactional
    public Movimiento registrarTraslado(Integer productoId, Long almacenOrigenId,
                                        Long almacenDestinoId, Integer cantidad, String motivo) {
        // Validaciones
        if (almacenOrigenId.equals(almacenDestinoId)) {
            throw new RuntimeException("El almacén de origen y destino no pueden ser el mismo");
        }

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Almacen almacenOrigen = almacenRepository.findById(almacenOrigenId)
                .orElseThrow(() -> new RuntimeException("Almacén de origen no encontrado"));

        Almacen almacenDestino = almacenRepository.findById(almacenDestinoId)
                .orElseThrow(() -> new RuntimeException("Almacén de destino no encontrado"));

        // Verificar stock en origen
        ProductoAlmacen productoOrigen = productoAlmacenRepository
                .findByProductoIdAndAlmacenId(productoId, almacenOrigenId)
                .orElseThrow(() -> new RuntimeException("Producto no existe en almacén de origen"));

        if (productoOrigen.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente en almacén de origen. Disponible: " + productoOrigen.getStock());
        }

        // Descontar del origen
        productoOrigen.setStock(productoOrigen.getStock() - cantidad);
        productoAlmacenRepository.save(productoOrigen);

        // Agregar al destino
        ProductoAlmacen productoDestino = productoAlmacenRepository
                .findByProductoIdAndAlmacenId(productoId, almacenDestinoId)
                .orElseGet(() -> {
                    ProductoAlmacen nuevo = new ProductoAlmacen();
                    nuevo.setProducto(producto);
                    nuevo.setAlmacen(almacenDestino);
                    nuevo.setStock(0);
                    nuevo.setActivo(true);
                    return nuevo;
                });

        productoDestino.setStock(productoDestino.getStock() + cantidad);
        productoAlmacenRepository.save(productoDestino);

        // Actualizar stock total del producto
        producto.calcularStockTotal();
        productoRepository.save(producto);

        // Crear movimiento
        Movimiento movimiento = new Movimiento();
        movimiento.setCodigo(generarCodigoMovimiento());
        movimiento.setProducto(producto);
        movimiento.setAlmacenOrigen(almacenOrigen);
        movimiento.setAlmacenDestino(almacenDestino);
        movimiento.setTipoMovimiento(TipoMovimiento.TRASLADO);
        movimiento.setCantidad(cantidad);
        movimiento.setMotivo(motivo);
        movimiento.setFechaMovimiento(LocalDateTime.now());

        return movimientoRepository.save(movimiento);
    }

    @Override
    @Transactional
    public Movimiento registrarEntrada(Integer productoId, Long almacenDestinoId,
                                       Integer cantidad, String motivo) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Almacen almacenDestino = almacenRepository.findById(almacenDestinoId)
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado"));

        // Buscar o crear ProductoAlmacen
        ProductoAlmacen productoAlmacen = productoAlmacenRepository
                .findByProductoIdAndAlmacenId(productoId, almacenDestinoId)
                .orElseGet(() -> {
                    ProductoAlmacen nuevo = new ProductoAlmacen();
                    nuevo.setProducto(producto);
                    nuevo.setAlmacen(almacenDestino);
                    nuevo.setStock(0);
                    nuevo.setActivo(true);
                    return nuevo;
                });

        productoAlmacen.setStock(productoAlmacen.getStock() + cantidad);
        productoAlmacenRepository.save(productoAlmacen);

        // Actualizar stock total
        producto.calcularStockTotal();
        productoRepository.save(producto);

        // Crear movimiento
        Movimiento movimiento = new Movimiento();
        movimiento.setCodigo(generarCodigoMovimiento());
        movimiento.setProducto(producto);
        movimiento.setAlmacenDestino(almacenDestino);
        movimiento.setTipoMovimiento(TipoMovimiento.ENTRADA);
        movimiento.setCantidad(cantidad);
        movimiento.setMotivo(motivo);
        movimiento.setFechaMovimiento(LocalDateTime.now());

        return movimientoRepository.save(movimiento);
    }

    @Override
    @Transactional
    public Movimiento registrarSalida(Integer productoId, Long almacenOrigenId,
                                      Integer cantidad, String motivo) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Almacen almacenOrigen = almacenRepository.findById(almacenOrigenId)
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado"));

        ProductoAlmacen productoAlmacen = productoAlmacenRepository
                .findByProductoIdAndAlmacenId(productoId, almacenOrigenId)
                .orElseThrow(() -> new RuntimeException("Producto no existe en este almacén"));

        if (productoAlmacen.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente. Disponible: " + productoAlmacen.getStock());
        }

        productoAlmacen.setStock(productoAlmacen.getStock() - cantidad);
        productoAlmacenRepository.save(productoAlmacen);

        // Actualizar stock total
        producto.calcularStockTotal();
        productoRepository.save(producto);

        // Crear movimiento
        Movimiento movimiento = new Movimiento();
        movimiento.setCodigo(generarCodigoMovimiento());
        movimiento.setProducto(producto);
        movimiento.setAlmacenOrigen(almacenOrigen);
        movimiento.setTipoMovimiento(TipoMovimiento.SALIDA);
        movimiento.setCantidad(cantidad);
        movimiento.setMotivo(motivo);
        movimiento.setFechaMovimiento(LocalDateTime.now());

        return movimientoRepository.save(movimiento);
    }

    @Override
    @Transactional
    public Movimiento registrarAjuste(Integer productoId, Long almacenId,
                                      Integer cantidad, String motivo) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Almacen almacen = almacenRepository.findById(almacenId)
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado"));

        ProductoAlmacen productoAlmacen = productoAlmacenRepository
                .findByProductoIdAndAlmacenId(productoId, almacenId)
                .orElseThrow(() -> new RuntimeException("Producto no existe en este almacén"));

        int stockAnterior = productoAlmacen.getStock();
        productoAlmacen.setStock(stockAnterior + cantidad);
        productoAlmacenRepository.save(productoAlmacen);

        // Actualizar stock total
        producto.calcularStockTotal();
        productoRepository.save(producto);

        // Crear movimiento
        Movimiento movimiento = new Movimiento();
        movimiento.setCodigo(generarCodigoMovimiento());
        movimiento.setProducto(producto);

        if (cantidad > 0) {
            movimiento.setAlmacenDestino(almacen);
        } else {
            movimiento.setAlmacenOrigen(almacen);
        }

        movimiento.setTipoMovimiento(TipoMovimiento.AJUSTE);
        movimiento.setCantidad(Math.abs(cantidad));
        movimiento.setMotivo(motivo);
        movimiento.setFechaMovimiento(LocalDateTime.now());

        return movimientoRepository.save(movimiento);
    }

    @Override
    public List<Movimiento> listarTodos() {
        return movimientoRepository.findAll();
    }

    @Override
    public List<Movimiento> listarPorTipo(TipoMovimiento tipo) {
        return movimientoRepository.findByTipoMovimientoOrderByFechaMovimientoDesc(tipo);
    }

    @Override
    public List<Movimiento> listarPorProducto(Integer productoId) {
        return movimientoRepository.findByProductoIdOrderByFechaMovimientoDesc(productoId);
    }

    @Override
    public List<Movimiento> listarPorAlmacen(Long almacenId) {
        return movimientoRepository.findMovimientosPorAlmacen(almacenId);
    }

    @Override
    public List<Movimiento> listarPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return movimientoRepository.findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(inicio, fin);
    }

    @Override
    public Movimiento obtenerPorId(Long id) {
        return movimientoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado"));
    }

    // Método auxiliar para generar código
    private String generarCodigoMovimiento() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String ultimoCodigo = movimientoRepository.findUltimoCodigo().orElse("MOV-" + fecha + "-0000");

        String[] partes = ultimoCodigo.split("-");
        int numero = Integer.parseInt(partes[2]) + 1;

        return String.format("MOV-%s-%04d", fecha, numero);
    }
}