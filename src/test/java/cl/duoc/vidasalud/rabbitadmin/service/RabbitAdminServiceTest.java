package cl.duoc.vidasalud.rabbitadmin.service;

import cl.duoc.vidasalud.rabbitadmin.config.AdminPropiedades;
import cl.duoc.vidasalud.rabbitadmin.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RabbitAdminServiceTest {

    private final RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);
    private final RabbitAdminService service =
            new RabbitAdminService(rabbitAdmin, new AdminPropiedades("vidasalud.dlx", ".dlq"));

    @Test
    void creaColaConDlqConfigurada() {
        when(rabbitAdmin.getQueueInfo("q.demo")).thenReturn(new QueueInformation("q.demo", 0, 0));

        service.crearCola("q.demo", true, true);

        ArgumentCaptor<Queue> colas = ArgumentCaptor.forClass(Queue.class);
        verify(rabbitAdmin, times(2)).declareQueue(colas.capture());
        List<Queue> declaradas = colas.getAllValues();

        assertThat(declaradas.get(0).getName()).isEqualTo("q.demo.dlq");
        Queue principal = declaradas.get(1);
        assertThat(principal.getName()).isEqualTo("q.demo");
        assertThat(principal.getArguments())
                .containsEntry("x-dead-letter-exchange", "vidasalud.dlx")
                .containsEntry("x-dead-letter-routing-key", "q.demo.dlq");
    }

    @Test
    void verColaInexistenteDa404() {
        when(rabbitAdmin.getQueueInfo("q.no.existe")).thenReturn(null);

        assertThatThrownBy(() -> service.infoCola("q.no.existe"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void noEliminaColaQueNoExiste() {
        when(rabbitAdmin.getQueueInfo("q.no.existe")).thenReturn(null);

        assertThatThrownBy(() -> service.eliminarCola("q.no.existe"))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(rabbitAdmin, never()).deleteQueue(anyString());
    }
}
