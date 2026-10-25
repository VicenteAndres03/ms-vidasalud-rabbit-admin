package cl.duoc.vidasalud.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class RabbitDtos {

    /** Letras, números, punto, guion y guion bajo; 3 a 100 caracteres; no puede empezar con "amq." (reservado). */
    public static final String NOMBRE_VALIDO = "^(?!amq\\.)[A-Za-z0-9._-]{3,100}$";
    public static final String MENSAJE_NOMBRE =
            "debe tener entre 3 y 100 caracteres (letras, números, '.', '-', '_') y no empezar con 'amq.'";

    public record CrearColaRequest(
            @NotBlank @Pattern(regexp = NOMBRE_VALIDO, message = MENSAJE_NOMBRE) String nombre,
            Boolean durable,
            Boolean conDlq
    ) {}

    public record CrearExchangeRequest(
            @NotBlank @Pattern(regexp = NOMBRE_VALIDO, message = MENSAJE_NOMBRE) String nombre,
            @NotBlank @Pattern(regexp = "direct|topic|fanout", message = "debe ser direct, topic o fanout") String tipo,
            Boolean durable
    ) {}

    public record BindingRequest(
            @NotBlank @Pattern(regexp = NOMBRE_VALIDO, message = MENSAJE_NOMBRE) String cola,
            @NotBlank @Pattern(regexp = NOMBRE_VALIDO, message = MENSAJE_NOMBRE) String exchange,
            @NotNull @Pattern(regexp = "^[A-Za-z0-9._*#-]{0,255}$",
                    message = "solo letras, números, '.', '-', '_', '*' y '#' (máx. 255)") String routingKey
    ) {}

    public record ColaInfoResponse(String nombre, long mensajes, int consumidores) {}

    public record MensajeResponse(String mensaje) {}
}
