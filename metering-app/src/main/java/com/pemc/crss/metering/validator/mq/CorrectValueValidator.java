package com.pemc.crss.metering.validator.mq;

import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import com.pemc.crss.metering.validator.ValidationResult;
import com.pemc.crss.metering.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;

@Slf4j
@Component
@Order(value = 2)
public class CorrectValueValidator implements Validator {

    @Override
    public ValidationResult validate(FileManifest fileManifest, MeterData meterData) {
        ValidationResult retVal = new ValidationResult();
        retVal.setStatus(ACCEPTED);

        for (MeterDataDetail meterDataDetail : meterData.getDetails()) {
            if (meterDataDetail.getEstimationFlag() != null && !meterDataDetail.getEstimationFlag().equals("E")) {
                retVal.setStatus(REJECTED);
                retVal.setErrorDetail("Incorrect estimation flag value. Estimation flag value can be blank or 'E' only");
            }
        }
        return retVal;
    }
}
