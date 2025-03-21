package com.imageprocessor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Async
    public void sendVerificationEmail(String to, String token) {
        String verificationUrl = baseUrl + "/api/auth/verify?token=" + token;
        log.info("Simulando envio de e-mail para: {} com URL de verificação: {}", to, verificationUrl);
        // Em ambiente de desenvolvimento, apenas registramos o e-mail que seria enviado
    }

    @Async
    public void sendProcessingCompletedEmail(String to, String imageFilename, String resultUrl) {
        log.info("Simulando envio de e-mail para: {} com resultado de processamento da imagem: {}, URL: {}",
                to, imageFilename, resultUrl);
        // Em ambiente de desenvolvimento, apenas registramos o e-mail que seria enviado
    }
}