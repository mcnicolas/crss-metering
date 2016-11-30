package com.pemc.crss.metering.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static org.springframework.amqp.core.ExchangeTypes.DIRECT;

@Slf4j
@Component
public class MQCleanupListener {

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "crss.mq.cleanup", durable = "true"),
            exchange = @Exchange(type = DIRECT, value = "crss.mq"),
            key = "crss.mq.cleanup"))
    public void processMQCleanup() {
        log.debug("Received MQ cleanup trigger.");
    }

}
