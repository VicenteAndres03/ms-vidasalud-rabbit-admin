package cl.duoc.vidasalud.rabbitadmin.dto;

import cl.duoc.vidasalud.rabbitadmin.dto.RabbitDtos.CrearColaRequest;
import cl.duoc.vidasalud.rabbitadmin.dto.RabbitDtos.CrearExchangeRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitDtosValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rechazaNombreVacio() {
        assertThat(validator.validate(new CrearColaRequest("", true, false))).isNotEmpty();
    }

    @Test
    void rechazaNombreReservadoAmq() {
        assertThat(validator.validate(new CrearColaRequest("amq.mia", true, false))).isNotEmpty();
    }

    @Test
    void rechazaCaracteresInvalidos() {
        assertThat(validator.validate(new CrearColaRequest("cola con espacios", true, false))).isNotEmpty();
    }

    @Test
    void aceptaNombreValido() {
        assertThat(validator.validate(new CrearColaRequest("q.demo-1", true, true))).isEmpty();
    }

    @Test
    void rechazaTipoDeExchangeDesconocido() {
        assertThat(validator.validate(new CrearExchangeRequest("demo.exchange", "headers", true))).isNotEmpty();
    }
}
