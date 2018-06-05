package com.pemc.crss.metering;

import com.pemc.crss.commons.logger.MDCLoggingSupport;
import com.pemc.crss.shared.core.config.cache.redis.RedisConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@Import({
        RedisConfig.class
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public MDCLoggingSupport MDCLoggingSupport() {
        return new MDCLoggingSupport();
    }
}
