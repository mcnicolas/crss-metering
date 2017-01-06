package com.pemc.crss.metering.listener;

import com.pemc.crss.metering.service.BcqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.springframework.amqp.core.ExchangeTypes.DIRECT;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BcqConfirmationListener {

    private final BcqService bcqService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "crss.scheduler.bcq.de_confirmation", durable = "true"),
            exchange = @Exchange(type = DIRECT, value = "crss.scheduler"),
            key = "crss.scheduler.bcq.de_confirmation"))
    public void processConfirmation() {
        log.debug("Received BCQ confirmation expiration trigger.");
        bcqService.processUnconfirmedHeaders();
        log.debug("Finished processing unconfirmed headers.");
    }

}
