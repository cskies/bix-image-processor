package com.imageprocessor.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendVerificationEmail(String to, String token) {
        try {
            String verificationUrl = baseUrl + "/api/auth/verify?token=" + token;

            Context context = new Context();
            context.setVariable("verificationUrl", verificationUrl);

            String content = templateEngine.process("email-confirmation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Confirme seu cadastro no Image Processor");
            helper.setText(content, true);

            mailSender.send(message);
            log.info("E-mail de verificação enviado para: {}", to);
        } catch (MessagingException e) {
            log.error("Erro ao enviar e-mail de verificação para: {}", to, e);
        }
    }

    @Async
    public void sendProcessingCompletedEmail(String to, String imageFilename, String resultUrl) {
        try {
            Context context = new Context();
            context.setVariable("imageFilename", imageFilename);
            context.setVariable("resultUrl", resultUrl);

            String content = templateEngine.process("processing-completed", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Seu processamento de imagem foi concluído");
            helper.setText(content, true);

            mailSender.send(message);
            log.info("E-mail de processamento concluído enviado para: {}", to);
        } catch (MessagingException e) {
            log.error("Erro ao enviar e-mail de processamento concluído para: {}", to, e);
        }
    }
}