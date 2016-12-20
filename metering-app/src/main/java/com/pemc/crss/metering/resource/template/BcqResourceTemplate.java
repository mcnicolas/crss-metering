package com.pemc.crss.metering.resource.template;

import org.omg.CORBA.Object;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BcqResourceTemplate extends ResourceTemplate {

    @Autowired
    public BcqResourceTemplate(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public Object getCurrentParticipant() {
        return get("/reg/participants/bcq/participant-name", Object.class);
    }

}
