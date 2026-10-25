package cl.duoc.vidasalud.rabbitadmin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbit")
public record AdminPropiedades(String exchangeDlx, String sufijoDlq) {
}
