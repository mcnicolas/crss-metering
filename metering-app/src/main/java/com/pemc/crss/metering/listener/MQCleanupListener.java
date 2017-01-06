package com.pemc.crss.metering.listener;

import com.pemc.crss.metering.service.MeterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.amqp.core.ExchangeTypes.DIRECT;

@Slf4j
@Component
public class MQCleanupListener extends AbstractNotificationListener {

    @Autowired
    private MeterService meterService;

    @RabbitListener(bindings = @QueueBinding(key = "crss.scheduler.mq.cleanup",
            value = @Queue(value = "crss.scheduler.mq.cleanup", durable = "true"),
            exchange = @Exchange(type = DIRECT, value = "crss.scheduler")))
    public void processMQCleanup() {
        log.debug("Received MQ cleanup trigger.");

        List<Long> staleRecords = meterService.getStaleRecords();
        log.debug("Stale Record count:{}", staleRecords.size());

        for (Long headerID : staleRecords) {
            notify(headerID);
        }
    }

}
