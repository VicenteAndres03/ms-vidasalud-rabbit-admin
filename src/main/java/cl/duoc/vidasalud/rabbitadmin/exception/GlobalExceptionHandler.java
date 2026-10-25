package cl.duoc.vidasalud.rabbitadmin.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacionBody(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("datos inválidos");
        return respuesta(HttpStatus.BAD_REQUEST, detalle);
    }

    /** Validación de parámetros de la URL (ej: DELETE /queues/{nombre}). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> validacionParametros(HandlerMethodValidationException ex) {
        String detalle = ex.getAllValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream())
                .map(e -> "nombre " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("parámetros inválidos");
        return respuesta(HttpStatus.BAD_REQUEST, detalle);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> validacionRestricciones(ConstraintViolationException ex) {
        String detalle = ex.getConstraintViolations().stream()
                .map(v -> "nombre " + v.getMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("parámetros inválidos");
        return respuesta(HttpStatus.BAD_REQUEST, detalle);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> jsonInvalido(HttpMessageNotReadableException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es un JSON válido");
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(RecursoNoEncontradoException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AmqpConnectException.class)
    public ResponseEntity<Map<String, Object>> sinConexion(AmqpConnectException ex) {
        log.error("Sin conexión a RabbitMQ: {}", ex.getMessage());
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, "RabbitMQ no está disponible");
    }

    /** RabbitMQ rechazó la operación (ej: el exchange del binding no existe). */
    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<Map<String, Object>> rechazadoPorRabbit(AmqpException ex) {
        String causa = NestedExceptionUtils.getMostSpecificCause(ex).getMessage();
        log.warn("RabbitMQ rechazó la operación: {}", causa);
        return respuesta(HttpStatus.CONFLICT, "RabbitMQ rechazó la operación: " + causa);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> generico(Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
            return respuesta(status, status.getReasonPhrase());
        }
        log.error("Error no controlado", ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno");
    }

    private ResponseEntity<Map<String, Object>> respuesta(HttpStatus status, String mensaje) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", mensaje);
        return ResponseEntity.status(status).body(body);
    }
}
