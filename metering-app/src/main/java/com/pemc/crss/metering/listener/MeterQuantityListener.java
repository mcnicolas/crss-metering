package com.pemc.crss.metering.listener;

import com.pemc.crss.metering.service.MeterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static org.springframework.amqp.core.ExchangeTypes.DIRECT;

@Slf4j
@Component
public class MeterQuantityListener {

    private final MeterService meterService;

    @Autowired
    public MeterQuantityListener(MeterService meterService) {
        this.meterService = meterService;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "crss.meter.quantity", durable = "true"),
            exchange = @Exchange(type = DIRECT, value = "crss.meter.quantity"),
            key = "crss.meter.quantity")
    )
    public void processMeterQuantityFile(@Header int headerID,
                                         @Header String transactionID,
                                         @Header String fileName,
                                         @Header String fileType,
                                         @Header long fileSize,
                                         @Header String checksum,
                                         @Header String mspShortName,
                                         @Header String category,
                                         @Payload byte[] fileContent) {

        log.debug("Received MQ File. headerID:{} txnID:{} fileName:{} checksum:{} mspShortName:{} category:{}",
                headerID, transactionID, fileName, checksum, mspShortName, category);

        try {
            long fileID = meterService.saveFileManifest(headerID, transactionID, fileName, fileType, fileSize, checksum);

            meterService.saveMeterData(fileID, fileType, fileContent, mspShortName, category);
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            // TODO: Explore using DLX/DLQ
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }

}
