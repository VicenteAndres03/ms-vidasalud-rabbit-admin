package cl.duoc.vidasalud.rabbitadmin.controller;

import cl.duoc.vidasalud.rabbitadmin.dto.RabbitDtos;
import cl.duoc.vidasalud.rabbitadmin.dto.RabbitDtos.*;
import cl.duoc.vidasalud.rabbitadmin.service.RabbitAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * API REST del administrador de RabbitMQ.
 *
 *   POST   /api/rabbit/queues            crear cola (opcionalmente con DLQ)
 *   GET    /api/rabbit/queues/{nombre}   ver mensajes y consumidores de una cola
 *   DELETE /api/rabbit/queues/{nombre}   eliminar cola
 *   POST   /api/rabbit/exchanges         crear exchange (direct, topic o fanout)
 *   DELETE /api/rabbit/exchanges/{nombre}
 *   POST   /api/rabbit/bindings          unir cola y exchange con una routing key
 *   DELETE /api/rabbit/bindings          eliminar ese binding
 */
@RestController
@RequestMapping("/api/rabbit")
@Validated
public class RabbitAdminController {

    private final RabbitAdminService service;

    public RabbitAdminController(RabbitAdminService service) {
        this.service = service;
    }

    @PostMapping("/queues")
    public ResponseEntity<ColaInfoResponse> crearCola(@Valid @RequestBody CrearColaRequest request) {
        ColaInfoResponse info = service.crearCola(
                request.nombre(),
                !Boolean.FALSE.equals(request.durable()),
                Boolean.TRUE.equals(request.conDlq()));
        return ResponseEntity.status(HttpStatus.CREATED).body(info);
    }

    @GetMapping("/queues/{nombre}")
    public ColaInfoResponse verCola(
            @PathVariable @Pattern(regexp = RabbitDtos.NOMBRE_VALIDO, message = RabbitDtos.MENSAJE_NOMBRE) String nombre) {
        return service.infoCola(nombre);
    }

    @DeleteMapping("/queues/{nombre}")
    public ResponseEntity<Void> eliminarCola(
            @PathVariable @Pattern(regexp = RabbitDtos.NOMBRE_VALIDO, message = RabbitDtos.MENSAJE_NOMBRE) String nombre) {
        service.eliminarCola(nombre);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/exchanges")
    public ResponseEntity<MensajeResponse> crearExchange(@Valid @RequestBody CrearExchangeRequest request) {
        service.crearExchange(request.nombre(), request.tipo(), !Boolean.FALSE.equals(request.durable()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MensajeResponse("Exchange '" + request.nombre() + "' creado (" + request.tipo() + ")"));
    }

    @DeleteMapping("/exchanges/{nombre}")
    public ResponseEntity<Void> eliminarExchange(
            @PathVariable @Pattern(regexp = RabbitDtos.NOMBRE_VALIDO, message = RabbitDtos.MENSAJE_NOMBRE) String nombre) {
        service.eliminarExchange(nombre);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bindings")
    public ResponseEntity<MensajeResponse> crearBinding(@Valid @RequestBody BindingRequest request) {
        service.crearBinding(request.cola(), request.exchange(), request.routingKey());
        return ResponseEntity.status(HttpStatus.CREATED).body(new MensajeResponse(
                "Binding creado: " + request.exchange() + " -> " + request.cola() + " [" + request.routingKey() + "]"));
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Void> eliminarBinding(@Valid @RequestBody BindingRequest request) {
        service.eliminarBinding(request.cola(), request.exchange(), request.routingKey());
        return ResponseEntity.noContent().build();
    }
}
