package com.pemc.crss.metering;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(RabbitConfigProperties.class)
public class RabbitConfig {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitConfig.class);

    @Autowired
    private RabbitConfigProperties configProperties;

    @Bean
    public SimpleMessageListenerContainer messageListenerContainer() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory());
        container.setQueueNames(configProperties.getQueueName());
        container.setMessageListener(listener());
        return container;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(configProperties.getHostname());
        connectionFactory.setUsername(configProperties.getUsername());
        connectionFactory.setPassword(configProperties.getPassword());
        return connectionFactory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        template.setRoutingKey(configProperties.getQueueName());
        template.setQueue(configProperties.getQueueName());
        return template;
    }

    @Bean
    public MessageListener listener() {
        return message ->
                LOG.debug("Received from queue: " + message.getMessageProperties().getHeaders().get("File name"));
    }

    @Bean
    public Queue queue() {
        return new Queue(configProperties.getQueueName());
    }
}
