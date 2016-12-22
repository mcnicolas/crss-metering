package com.pemc.crss.metering.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import static com.fasterxml.jackson.databind.DeserializationFeature.UNWRAP_ROOT_VALUE;
import static com.fasterxml.jackson.databind.SerializationFeature.WRAP_ROOT_VALUE;

@Configuration
public class RestConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        restTemplate.getMessageConverters().add(jsonConverter());
        return restTemplate;
    }

    @Bean
    public MappingJackson2HttpMessageConverter jsonConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.getObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(UNWRAP_ROOT_VALUE, false)
                .configure(WRAP_ROOT_VALUE, false);
        return converter;
    }

}
