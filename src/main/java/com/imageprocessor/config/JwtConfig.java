package com.imageprocessor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtConfig {
    private String secret = "chave_padrao_para_desenvolvimento_local_deve_ser_alterada_em_producao";
    private long expirationMs = 86400000; // 24 horas
    private String issuer = "image-processor-app";
}