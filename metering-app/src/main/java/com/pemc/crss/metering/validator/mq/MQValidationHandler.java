package com.pemc.crss.metering.validator.mq;

import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.validator.ValidationResult;
import com.pemc.crss.metering.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;

@Component
public class MQValidationHandler {

    private final List<Validator> validatorList;

    @Autowired
    public MQValidationHandler(List<Validator> validatorList) {
        this.validatorList = validatorList;
    }

    public ValidationResult validate(FileManifest fileManifest, MeterData meterData) {
        ValidationResult retVal = new ValidationResult();

        for (Validator validator : validatorList) {
            retVal = validator.validate(fileManifest, meterData);

            if (retVal.getStatus() == REJECTED) {
                break;
            }
        }

        return retVal;
    }

}
