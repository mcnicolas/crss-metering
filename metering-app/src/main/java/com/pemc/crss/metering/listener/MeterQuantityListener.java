package com.pemc.crss.metering.listener;

import com.pemc.crss.metering.constants.FileType;
import com.pemc.crss.metering.constants.UploadType;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.service.MeterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.apache.commons.io.output.NullOutputStream.NULL_OUTPUT_STREAM;
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
            value = @Queue(value = "crss.mq.data", durable = "true"),
            exchange = @Exchange(type = DIRECT, value = "crss.mq"),
            key = "crss.mq.data"))
    public void processMeterQuantityFile(@Header int headerID,
                                         @Header String transactionID,
                                         @Header String fileName,
                                         @Header String fileType,
                                         @Header long fileSize,
                                         @Header(name = "checksum") String receivedChecksum,
                                         @Header String mspShortName,
                                         @Header String category,
                                         @Payload byte[] fileContent) {
        log.debug("Received MQ File. fileName:{} headerID:{} txnID:{} checksum:{} mspShortName:{} category:{}",
                fileName, headerID, transactionID, receivedChecksum, mspShortName, category);

        String checksum = getChecksum(fileContent);

        // TODO: Should be coming from the controller instead
        FileManifest fileManifest = new FileManifest();
        fileManifest.setHeaderID(headerID);
        fileManifest.setTransactionID(transactionID);
        fileManifest.setFileName(fileName);
        fileManifest.setFileType(FileType.valueOf(fileType));
        fileManifest.setFileSize(fileSize);
        fileManifest.setChecksum(checksum);
        fileManifest.setRecvChecksum(receivedChecksum);
        fileManifest.setMspShortName(mspShortName);
        fileManifest.setUploadType(UploadType.valueOf(category));

        try {
            meterService.processMeterData(fileManifest, fileContent);
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            // TODO: Explore using DLX/DLQ
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }

    private String getChecksum(byte[] fileContent) {
        String retVal = "";

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            InputStream inputStream = new DigestInputStream(new ByteArrayInputStream(fileContent), md);
            IOUtils.copy(inputStream, NULL_OUTPUT_STREAM);
            retVal = bytesToHex(md.digest());
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error(e.getMessage(), e);
        }

        return retVal;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte aByte : bytes) {
            String byteValue = Integer.toHexString(0xFF & aByte);
            hexString.append(byteValue.length() == 2 ? byteValue : "0" + byteValue);
        }
        return hexString.toString();
    }

}
