package com.imageprocessor.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_IMAGE_PROCESSING = "image-processing-queue";
    public static final String EXCHANGE_IMAGE_PROCESSING = "image-processing-exchange";
    public static final String ROUTING_KEY_IMAGE_PROCESSING = "image-processing";

    @Bean
    public Queue imageProcessingQueue() {
        return new Queue(QUEUE_IMAGE_PROCESSING, true);
    }

    @Bean
    public DirectExchange imageProcessingExchange() {
        return new DirectExchange(EXCHANGE_IMAGE_PROCESSING);
    }

    @Bean
    public Binding imageProcessingBinding(Queue imageProcessingQueue, DirectExchange imageProcessingExchange) {
        return BindingBuilder.bind(imageProcessingQueue)
                .to(imageProcessingExchange)
                .with(ROUTING_KEY_IMAGE_PROCESSING);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}