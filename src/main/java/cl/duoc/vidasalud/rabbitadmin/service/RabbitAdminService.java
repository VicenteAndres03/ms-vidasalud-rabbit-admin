package cl.duoc.vidasalud.rabbitadmin.service;

import cl.duoc.vidasalud.rabbitadmin.config.AdminPropiedades;
import cl.duoc.vidasalud.rabbitadmin.dto.RabbitDtos.ColaInfoResponse;
import cl.duoc.vidasalud.rabbitadmin.exception.RecursoNoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

/**
 * Toda la administración de RabbitMQ pasa por aquí. El controller solo llama
 * a estos métodos y no conoce los detalles de RabbitAdmin.
 */
@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);

    private final RabbitAdmin rabbitAdmin;
    private final AdminPropiedades props;

    public RabbitAdminService(RabbitAdmin rabbitAdmin, AdminPropiedades props) {
        this.rabbitAdmin = rabbitAdmin;
        this.props = props;
    }

    // ---------- colas ----------

    public ColaInfoResponse crearCola(String nombre, boolean durable, boolean conDlq) {
        QueueBuilder builder = durable ? QueueBuilder.durable(nombre) : QueueBuilder.nonDurable(nombre);

        if (conDlq) {
            String dlq = nombre + props.sufijoDlq();
            Exchange dlx = ExchangeBuilder.directExchange(props.exchangeDlx()).durable(true).build();
            Queue colaDlq = QueueBuilder.durable(dlq).build();

            rabbitAdmin.declareExchange(dlx);
            rabbitAdmin.declareQueue(colaDlq);
            rabbitAdmin.declareBinding(new Binding(dlq, Binding.DestinationType.QUEUE, props.exchangeDlx(), dlq, null));

            builder.deadLetterExchange(props.exchangeDlx()).deadLetterRoutingKey(dlq);
            log.info("DLQ {} creada para la cola {}", dlq, nombre);
        }

        rabbitAdmin.declareQueue(builder.build());
        log.info("Cola creada: {} (durable={}, conDlq={})", nombre, durable, conDlq);
        return infoCola(nombre);
    }

    public ColaInfoResponse infoCola(String nombre) {
        QueueInformation info = rabbitAdmin.getQueueInfo(nombre);
        if (info == null) {
            throw new RecursoNoEncontradoException("La cola '" + nombre + "' no existe");
        }
        return new ColaInfoResponse(info.getName(), info.getMessageCount(), info.getConsumerCount());
    }

    public void eliminarCola(String nombre) {
        infoCola(nombre); // 404 si no existe
        rabbitAdmin.deleteQueue(nombre);
        log.info("Cola eliminada: {}", nombre);
    }

    // ---------- exchanges ----------

    public void crearExchange(String nombre, String tipo, boolean durable) {
        ExchangeBuilder builder = switch (tipo) {
            case "direct" -> ExchangeBuilder.directExchange(nombre);
            case "topic" -> ExchangeBuilder.topicExchange(nombre);
            case "fanout" -> ExchangeBuilder.fanoutExchange(nombre);
            default -> throw new IllegalArgumentException("Tipo de exchange no soportado: " + tipo);
        };
        Exchange exchange = builder.durable(durable).build();
        rabbitAdmin.declareExchange(exchange);
        log.info("Exchange creado: {} ({}, durable={})", nombre, tipo, durable);
    }

    public void eliminarExchange(String nombre) {
        rabbitAdmin.deleteExchange(nombre);
        log.info("Exchange eliminado: {}", nombre);
    }

    // ---------- bindings ----------

    public void crearBinding(String cola, String exchange, String routingKey) {
        rabbitAdmin.declareBinding(binding(cola, exchange, routingKey));
        log.info("Binding creado: {} -> {} [{}]", exchange, cola, routingKey);
    }

    public void eliminarBinding(String cola, String exchange, String routingKey) {
        rabbitAdmin.removeBinding(binding(cola, exchange, routingKey));
        log.info("Binding eliminado: {} -> {} [{}]", exchange, cola, routingKey);
    }

    private Binding binding(String cola, String exchange, String routingKey) {
        return new Binding(cola, Binding.DestinationType.QUEUE, exchange, routingKey, null);
    }
}
