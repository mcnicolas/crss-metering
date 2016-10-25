package com.pemc.crss.metering.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitConfig.class);

    private static final String HOST_NAME = "localhost";
    private static final String USERNAME = "guest";
    private static final String PASSWORD = "guest";
    private static final String QUEUE_NAME = "meter.quantity";

    @Bean
    public SimpleMessageListenerContainer messageListenerContainer() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory());
        container.setQueueNames(QUEUE_NAME);
        container.setMessageListener(listener());
        return container;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(HOST_NAME);
        connectionFactory.setUsername(USERNAME);
        connectionFactory.setPassword(PASSWORD);
        return connectionFactory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        template.setRoutingKey(QUEUE_NAME);
        template.setQueue(QUEUE_NAME);
        return template;
    }

    @Bean
    public MessageListener listener() {
        return message ->
                LOG.debug("Received from queue: " + message.getMessageProperties().getHeaders().get("File name"));
    }

    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME);
    }
}
