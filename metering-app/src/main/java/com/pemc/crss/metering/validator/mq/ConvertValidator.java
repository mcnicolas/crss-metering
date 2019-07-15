package com.pemc.crss.metering.validator.mq;

import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.validator.ValidationResult;
import com.pemc.crss.metering.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;

@Slf4j
@Component
@Order(value = 6)
public class ConvertValidator implements Validator {

    @Override
    public ValidationResult validate(FileManifest fileManifest, MeterData meterData) {
        ValidationResult retVal = new ValidationResult();
        retVal.setStatus(ACCEPTED);

        long firstRecord = meterData.getDetails().get(0).getReadingDateTime();
        long secondRecord = meterData.getDetails().get(1).getReadingDateTime();
        long diff = secondRecord - firstRecord;

        if (meterData.isConvertToFiveMin() && diff == 5) {
            retVal.setStatus(REJECTED);
            retVal.setErrorDetail("Conversion can only be applied for files with 15 minute interval");
        }

        return retVal;
    }
}
