package com.pemc.crss.metering;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

    public static final String ROUTING_KEY = "crss.config.request";
    public static final String EXCHANGE = "crss.config";
    public static final String REPLY_QUEUE = "crss.config.response.metering";

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public ApplicationStartup(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Message message = MessageBuilder
                .withBody(new byte[]{})
                .setHeader("replyTo", REPLY_QUEUE)
                .build();
        rabbitTemplate.send(EXCHANGE, ROUTING_KEY, message);
    }

}
