package com.pemc.crss.metering.validator.mq;

import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.validator.ValidationResult;
import com.pemc.crss.metering.validator.Validator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Component
@Order(value = 1)
public class ChecksumValidator implements Validator {

    @Override
    public ValidationResult validate(FileManifest fileManifest, MeterData meterData) {
        ValidationResult retVal = new ValidationResult();

        if (equalsIgnoreCase(fileManifest.getRecvChecksum(), fileManifest.getChecksum())) {
            retVal.setStatus(ACCEPTED);
        } else {
            retVal.setStatus(REJECTED);
            String errorDetail = "Checksum did not match. Received:" + fileManifest.getRecvChecksum()
                    + " but got actual checksum:" + fileManifest.getChecksum();
            retVal.setErrorDetail(errorDetail);
        }

        return retVal;
    }

}
