package com.pemc.crss.metering.listener;

import com.pemc.crss.metering.dao.BcqDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.pemc.crss.metering.constants.BcqStatus.CONFIRMED;
import static com.pemc.crss.metering.constants.BcqStatus.FOR_NULLIFICATION;
import static org.springframework.amqp.core.ExchangeTypes.DIRECT;

@Slf4j
@Component
public class BcqNullificationListener {

    private final BcqDao bcqDao;

    @Autowired
    public BcqNullificationListener(BcqDao bcqDao) {
        this.bcqDao = bcqDao;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "crss.scheduler.bcq.de_nullification", durable = "true"),
            exchange = @Exchange(type = DIRECT, value = "crss.scheduler"),
            key = "crss.scheduler.bcq.de_nullification"))
    public void processNullification() {
        log.debug("Received BCQ nullification expiration trigger.");

        Map<String, String> params = new HashMap<>();
        params.put("expired", "expired");
        params.put("status", FOR_NULLIFICATION.toString());
        bcqDao.findAllHeaders(params).forEach(header ->
                bcqDao.updateHeaderStatus(header.getHeaderId(), CONFIRMED));
    }

}
