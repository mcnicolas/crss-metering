package com.pemc.crss.metering.listener;

import com.pemc.crss.metering.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.springframework.amqp.core.ExchangeTypes.FANOUT;
import static org.springframework.amqp.core.ExchangeTypes.TOPIC;

@Slf4j
@Component
public class ConfigurationListener {

    private final CacheService cacheService;

    @Autowired
    public ConfigurationListener(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(),
            exchange = @Exchange(value = "crss.config.update", type = FANOUT)))
    public void updateConfiguration(Map<String, String> config) {
        log.debug("Received update configuration");
        updateCache(config);
    }

    @RabbitListener(bindings = @QueueBinding(key = "crss.config.*.metering",
            value = @Queue(),
            exchange = @Exchange(type = TOPIC, value = "crss.config")))
    public void initialConfiguration(Map<String, String> config) {
        log.info("Received initial system configuration");
        updateCache(config);
    }

    private void updateCache(Map<String, String> config) {
        config.forEach((k, v) -> cacheService.updateConfig(k, v));
    }

}
