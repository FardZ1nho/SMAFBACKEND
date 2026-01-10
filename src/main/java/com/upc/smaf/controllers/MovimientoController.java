package com.upc.smaf.controllers;

import com.upc.smaf.dtos.response.MovimientoResponseDTO;
import com.upc.smaf.entities.Movimiento;
import com.upc.smaf.entities.Movimiento.TipoMovimiento;
import com.upc.smaf.serviceinterface.MovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/movimientos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;

    /**
     * Registrar traslado entre almacenes
     * POST /movimientos/traslado
     */
    @PostMapping("/traslado")
    public ResponseEntity<?> registrarTraslado(@RequestBody Map<String, Object> request) {
        try {
            Integer productoId = (Integer) request.get("productoId");
            Long almacenOrigenId = ((Number) request.get("almacenOrigenId")).longValue();
            Long almacenDestinoId = ((Number) request.get("almacenDestinoId")).longValue();
            Integer cantidad = (Integer) request.get("cantidad");
            String motivo = (String) request.get("motivo");

            Movimiento movimiento = movimientoService.registrarTraslado(
                    productoId, almacenOrigenId, almacenDestinoId, cantidad, motivo);

            // ⭐ CONVERTIR A DTO
            MovimientoResponseDTO dto = convertirADTO(movimiento);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Registrar entrada de mercancía
     * POST /movimientos/entrada
     */
    @PostMapping("/entrada")
    public ResponseEntity<?> registrarEntrada(@RequestBody Map<String, Object> request) {
        try {
            Integer productoId = (Integer) request.get("productoId");
            Long almacenDestinoId = ((Number) request.get("almacenDestinoId")).longValue();
            Integer cantidad = (Integer) request.get("cantidad");
            String motivo = (String) request.get("motivo");

            Movimiento movimiento = movimientoService.registrarEntrada(
                    productoId, almacenDestinoId, cantidad, motivo);

            // ⭐ CONVERTIR A DTO
            MovimientoResponseDTO dto = convertirADTO(movimiento);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Registrar salida de mercancía
     * POST /movimientos/salida
     */
    @PostMapping("/salida")
    public ResponseEntity<?> registrarSalida(@RequestBody Map<String, Object> request) {
        try {
            Integer productoId = (Integer) request.get("productoId");
            Long almacenOrigenId = ((Number) request.get("almacenOrigenId")).longValue();
            Integer cantidad = (Integer) request.get("cantidad");
            String motivo = (String) request.get("motivo");

            Movimiento movimiento = movimientoService.registrarSalida(
                    productoId, almacenOrigenId, cantidad, motivo);

            // ⭐ CONVERTIR A DTO
            MovimientoResponseDTO dto = convertirADTO(movimiento);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Registrar ajuste de inventario
     * POST /movimientos/ajuste
     */
    @PostMapping("/ajuste")
    public ResponseEntity<?> registrarAjuste(@RequestBody Map<String, Object> request) {
        try {
            Integer productoId = (Integer) request.get("productoId");
            Long almacenId = ((Number) request.get("almacenId")).longValue();
            Integer cantidad = (Integer) request.get("cantidad");
            String motivo = (String) request.get("motivo");

            Movimiento movimiento = movimientoService.registrarAjuste(
                    productoId, almacenId, cantidad, motivo);

            // ⭐ CONVERTIR A DTO
            MovimientoResponseDTO dto = convertirADTO(movimiento);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Listar todos los movimientos
     * GET /movimientos
     */
    @GetMapping
    public ResponseEntity<List<MovimientoResponseDTO>> listarTodos() {
        List<Movimiento> movimientos = movimientoService.listarTodos();
        List<MovimientoResponseDTO> dtos = movimientos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Listar por tipo
     * GET /movimientos/tipo/{tipo}
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<MovimientoResponseDTO>> listarPorTipo(@PathVariable TipoMovimiento tipo) {
        List<Movimiento> movimientos = movimientoService.listarPorTipo(tipo);
        List<MovimientoResponseDTO> dtos = movimientos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Listar por producto
     * GET /movimientos/producto/{productoId}
     */
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<MovimientoResponseDTO>> listarPorProducto(@PathVariable Integer productoId) {
        List<Movimiento> movimientos = movimientoService.listarPorProducto(productoId);
        List<MovimientoResponseDTO> dtos = movimientos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Listar por almacén
     * GET /movimientos/almacen/{almacenId}
     */
    @GetMapping("/almacen/{almacenId}")
    public ResponseEntity<List<MovimientoResponseDTO>> listarPorAlmacen(@PathVariable Long almacenId) {
        List<Movimiento> movimientos = movimientoService.listarPorAlmacen(almacenId);
        List<MovimientoResponseDTO> dtos = movimientos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtener por ID
     * GET /movimientos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            Movimiento movimiento = movimientoService.obtenerPorId(id);
            MovimientoResponseDTO dto = convertirADTO(movimiento);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ================================================================
    // MÉTODO AUXILIAR PARA CONVERTIR ENTIDAD A DTO
    // ================================================================
    private MovimientoResponseDTO convertirADTO(Movimiento mov) {
        MovimientoResponseDTO dto = new MovimientoResponseDTO();

        dto.setId(mov.getId());
        dto.setCodigo(mov.getCodigo());

        // Producto (acceso seguro)
        if (mov.getProducto() != null) {
            dto.setProductoId(mov.getProducto().getId());
            dto.setProductoNombre(mov.getProducto().getNombre());
            dto.setProductoCodigo(mov.getProducto().getCodigo());
        }

        // Almacén Origen (opcional)
        if (mov.getAlmacenOrigen() != null) {
            dto.setAlmacenOrigenId(mov.getAlmacenOrigen().getId());
            dto.setAlmacenOrigenNombre(mov.getAlmacenOrigen().getNombre());
        }

        // Almacén Destino (opcional)
        if (mov.getAlmacenDestino() != null) {
            dto.setAlmacenDestinoId(mov.getAlmacenDestino().getId());
            dto.setAlmacenDestinoNombre(mov.getAlmacenDestino().getNombre());
        }

        // Tipo de movimiento
        dto.setTipoMovimiento(mov.getTipoMovimiento());
        dto.setTipoMovimientoLabel(obtenerLabelTipo(mov.getTipoMovimiento()));

        // Otros datos
        dto.setCantidad(mov.getCantidad());
        dto.setMotivo(mov.getMotivo());
        dto.setFechaMovimiento(mov.getFechaMovimiento());
        dto.setFechaCreacion(mov.getFechaCreacion());

        return dto;
    }

    private String obtenerLabelTipo(TipoMovimiento tipo) {
        switch (tipo) {
            case ENTRADA: return "Entrada";
            case SALIDA: return "Salida";
            case TRASLADO: return "Traslado";
            case AJUSTE: return "Ajuste";
            default: return tipo.name();
        }
    }
}