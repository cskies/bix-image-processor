package com.imageprocessor.queue;

import com.imageprocessor.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImageProcessingProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendProcessingTask(Long taskId) {
        log.info("Enviando tarefa para processamento: {}", taskId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_IMAGE_PROCESSING,
                RabbitMQConfig.ROUTING_KEY_IMAGE_PROCESSING,
                taskId
        );
    }
}