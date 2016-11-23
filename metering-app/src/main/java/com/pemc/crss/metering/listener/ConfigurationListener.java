package com.pemc.crss.metering.listener;

import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class ConfigurationListener {

    private final CacheManager cacheManager;

    @Autowired
    public ConfigurationListener(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "crss.config.update", durable = "true"),
            exchange = @Exchange(type = ExchangeTypes.FANOUT, value = "crss.config.update"))
    )
    public void updateConfiguration(@Payload byte[] config) {
        updateCache(config);
    }

    // TODO: Revisit exchange type. Requirement is to target specific service instance who initiated the config request.
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "crss.config.response.metering", durable = "true"),
            exchange = @Exchange(type = ExchangeTypes.TOPIC, value = "crss.config"),
            key = "crss.config.response.metering")
    )
    public void updateCache(@Payload byte[] config) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // TODO: Improve handling of json data
            Map<String, String> values = mapper.readValue(config, Map.class);

            Cache cache = cacheManager.getCache("config");
            values.forEach((k, v) -> cache.put(k, v));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

}
