# ms-vidasalud-rabbit-admin

Microservicio administrador de RabbitMQ (puerto 8084). Toda la lógica está en `RabbitAdminService`.

| Método | Ruta | Body |
|---|---|---|
| POST | `/api/rabbit/queues` | `{"nombre":"q.demo","durable":true,"conDlq":true}` |
| GET | `/api/rabbit/queues/{nombre}` | — |
| DELETE | `/api/rabbit/queues/{nombre}` | — |
| POST | `/api/rabbit/exchanges` | `{"nombre":"demo.exchange","tipo":"direct","durable":true}` |
| DELETE | `/api/rabbit/exchanges/{nombre}` | — |
| POST | `/api/rabbit/bindings` | `{"cola":"q.demo","exchange":"demo.exchange","routingKey":"demo"}` |
| DELETE | `/api/rabbit/bindings` | igual que el POST |

Los nombres se validan (3 a 100 caracteres, letras, números, `.`, `-`, `_`; no pueden empezar con `amq.`). Los errores responden 400, 404, 409 o 503 con un mensaje claro.

Variables: `RABBIT_ADDRESSES`, `RABBIT_USER`, `RABBIT_PASS`.
