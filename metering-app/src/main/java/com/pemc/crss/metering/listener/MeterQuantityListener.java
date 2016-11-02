package com.pemc.crss.metering.listener;

import com.pemc.crss.metering.service.MeterService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Component
public class MeterQuantityListener {

    @NonNull
    private final MeterService meterService;

    @Async
    @RabbitListener(queues = "meter.quantity")
    public void processMeterQuantityFile(@Header int headerID,
                                         @Header String transactionID,
                                         @Header String fileName,
                                         @Header String fileType,
                                         @Header long fileSize,
                                         @Header String checksum,
                                         @Header String category,
                                         @Payload byte[] fileContent) {

        try {
            long fileID = meterService.saveFileManifest(headerID, transactionID, fileName, fileType, fileSize, checksum);

            meterService.saveMeterData(fileID, fileType, fileContent, category);
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            // TODO: Explore using DLX/DLQ
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }

}
