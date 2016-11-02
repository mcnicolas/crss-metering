package com.pemc.crss.metering.listener;

import com.pemc.crss.metering.service.MeterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MeterQuantityListener {

    @Autowired
    private MeterService meterService;

    @Async
    @RabbitListener(queues = "meter.quantity")
    public void processMeterQuantityFile(@Header int headerID,
                                         @Header String transactionID,
                                         @Header String fileName,
                                         @Header String fileType,
                                         @Header long fileSize,
                                         @Header String checksum,
                                         @Payload byte[] fileContent) {
        meterService.saveFileManifest(headerID, transactionID, fileName, fileType, fileSize, checksum);

        // TODO: Parse the file
        log.debug("Message received...");
    }

}
