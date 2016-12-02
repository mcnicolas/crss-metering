package com.pemc.crss.metering.validator;

import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;

import java.util.List;

public interface Validator {

    ValidationResult validate(FileManifest fileManifest, MeterData meterData);

}
