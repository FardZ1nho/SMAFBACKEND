package com.upc.smaf.controllers;

import com.upc.smaf.dtos.request.AjusteRequestDTO; // ✅ Importar el DTO
import com.upc.smaf.dtos.response.MovimientoResponseDTO;
import com.upc.smaf.entities.Movimiento;
import com.upc.smaf.entities.Movimiento.TipoMovimiento;
import com.upc.smaf.serviceinterface.MovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/movimientos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;

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

            return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(movimiento));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/entrada")
    public ResponseEntity<?> registrarEntrada(@RequestBody Map<String, Object> request) {
        try {
            Integer productoId = (Integer) request.get("productoId");
            Long almacenDestinoId = ((Number) request.get("almacenDestinoId")).longValue();
            Integer cantidad = (Integer) request.get("cantidad");
            String motivo = (String) request.get("motivo");

            Movimiento movimiento = movimientoService.registrarEntrada(
                    productoId, almacenDestinoId, cantidad, motivo);

            return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(movimiento));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/salida")
    public ResponseEntity<?> registrarSalida(@RequestBody Map<String, Object> request) {
        try {
            Integer productoId = (Integer) request.get("productoId");
            Long almacenOrigenId = ((Number) request.get("almacenOrigenId")).longValue();
            Integer cantidad = (Integer) request.get("cantidad");
            String motivo = (String) request.get("motivo");

            Movimiento movimiento = movimientoService.registrarSalida(
                    productoId, almacenOrigenId, cantidad, motivo);

            return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(movimiento));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ MÉTODO CORREGIDO: Ahora usa el DTO y envía los 5 argumentos
    @PostMapping("/ajuste")
    public ResponseEntity<?> registrarAjuste(@RequestBody AjusteRequestDTO request) {
        try {
            if (request.getCantidad() == null || request.getCantidad() == 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "La cantidad no puede ser 0"));
            }
            if (request.getMotivo() == null || request.getMotivo().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El motivo es obligatorio"));
            }

            // Llamada al servicio con los 5 parámetros correctos
            Movimiento movimiento = movimientoService.registrarAjuste(
                    request.getProductoId(),
                    request.getAlmacenId(),
                    request.getCantidad(),
                    request.getMotivo(),
                    request.getUsuarioResponsable()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(movimiento));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<MovimientoResponseDTO>> listarTodos() {
        List<Movimiento> movimientos = movimientoService.listarTodos();
        List<MovimientoResponseDTO> dtos = movimientos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<MovimientoResponseDTO>> listarPorTipo(@PathVariable TipoMovimiento tipo) {
        return ResponseEntity.ok(movimientoService.listarPorTipo(tipo).stream()
                .map(this::convertirADTO).collect(Collectors.toList()));
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<MovimientoResponseDTO>> listarPorProducto(@PathVariable Integer productoId) {
        return ResponseEntity.ok(movimientoService.listarPorProducto(productoId).stream()
                .map(this::convertirADTO).collect(Collectors.toList()));
    }

    @GetMapping("/almacen/{almacenId}")
    public ResponseEntity<List<MovimientoResponseDTO>> listarPorAlmacen(@PathVariable Long almacenId) {
        return ResponseEntity.ok(movimientoService.listarPorAlmacen(almacenId).stream()
                .map(this::convertirADTO).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(convertirADTO(movimientoService.obtenerPorId(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private MovimientoResponseDTO convertirADTO(Movimiento mov) {
        MovimientoResponseDTO dto = new MovimientoResponseDTO();
        dto.setId(mov.getId());
        dto.setCodigo(mov.getCodigo());

        if (mov.getProducto() != null) {
            dto.setProductoId(mov.getProducto().getId());
            dto.setProductoNombre(mov.getProducto().getNombre());
            dto.setProductoCodigo(mov.getProducto().getCodigo());
        }

        if (mov.getAlmacenOrigen() != null) {
            dto.setAlmacenOrigenId(mov.getAlmacenOrigen().getId());
            dto.setAlmacenOrigenNombre(mov.getAlmacenOrigen().getNombre());
        }

        if (mov.getAlmacenDestino() != null) {
            dto.setAlmacenDestinoId(mov.getAlmacenDestino().getId());
            dto.setAlmacenDestinoNombre(mov.getAlmacenDestino().getNombre());
        }

        dto.setTipoMovimiento(mov.getTipoMovimiento());
        dto.setTipoMovimientoLabel(mov.getTipoMovimiento().name()); // O usar helper para labels bonitos
        dto.setCantidad(mov.getCantidad());
        dto.setMotivo(mov.getMotivo());

        // ✅ Asignar usuario responsable
        dto.setUsuarioResponsable(mov.getUsuarioResponsable());

        dto.setFechaMovimiento(mov.getFechaMovimiento());
        dto.setFechaCreacion(mov.getFechaCreacion());

        return dto;
    }
}