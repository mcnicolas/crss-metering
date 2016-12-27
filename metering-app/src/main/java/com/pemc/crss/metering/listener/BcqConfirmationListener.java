package com.pemc.crss.metering.listener;

import com.pemc.crss.metering.dao.BcqDao2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.pemc.crss.metering.constants.BcqStatus.FOR_CONFIRMATION;
import static com.pemc.crss.metering.constants.BcqStatus.NOT_CONFIRMED;
import static org.springframework.amqp.core.ExchangeTypes.DIRECT;

@Slf4j
@Component
public class BcqConfirmationListener {

    private final BcqDao2 bcqDao;

    @Autowired
    public BcqConfirmationListener(BcqDao2 bcqDao) {
        this.bcqDao = bcqDao;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "crss.scheduler.bcq.de_confirmation", durable = "true"),
            exchange = @Exchange(type = DIRECT, value = "crss.scheduler"),
            key = "crss.scheduler.bcq.de_confirmation"))
    public void processConfirmation() {
        log.debug("Received BCQ confirmation expiration trigger.");
        Map<String, String> params = new HashMap<>();
        params.put("expired", "expired");
        params.put("status", FOR_CONFIRMATION.toString());
        bcqDao.findAllHeaders(params).forEach(header ->
                bcqDao.updateHeaderStatus(header.getHeaderId(), NOT_CONFIRMED));
    }

}
