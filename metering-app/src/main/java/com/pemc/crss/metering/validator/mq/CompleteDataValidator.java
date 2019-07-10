package com.pemc.crss.metering.validator.mq;

import com.pemc.crss.metering.constants.FileType;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.service.CacheService;
import com.pemc.crss.metering.validator.ValidationResult;
import com.pemc.crss.metering.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static com.pemc.crss.metering.constants.FileType.CSV;
import static com.pemc.crss.metering.constants.FileType.XLS;
import static com.pemc.crss.metering.constants.UploadType.DAILY;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@Order(value = 2)
public class CompleteDataValidator implements Validator {

    private static final int DEFAULT_INTERVAL = 5;

    private final CacheService cacheService;

    @Autowired
    public CompleteDataValidator(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public ValidationResult validate(FileManifest fileManifest, MeterData meterData) {
        ValidationResult retVal = new ValidationResult();

        FileType fileType = fileManifest.getFileType();
        if ((fileType == XLS || fileType == CSV)
                && fileManifest.getUploadType() == DAILY) {

            int interval = DEFAULT_INTERVAL;
            String intervalObj = cacheService.getConfig("MQ_INTERVAL");
            if (isNotBlank(intervalObj)) {
                interval = Integer.parseInt(intervalObj);
            }

            int dataSize = meterData.getDetails().size();
            if (interval == 15 && dataSize != 96) {
                retVal.setStatus(REJECTED);
                retVal.setErrorDetail("Incorrect No. of Entries for Category Daily. MQ should have 96 entries.");
            } else if (interval == 5) {
                if (dataSize != 288) {
                    if (meterData.isConvertToFiveMin()) {
                        if (dataSize != 96) {
                            retVal.setStatus(REJECTED);
                            retVal.setErrorDetail("Incorrect No. of Entries for Category Daily. MQ should have 96/288 entries.");
                        }
                    } else {
                        retVal.setStatus(REJECTED);
                        retVal.setErrorDetail("Incorrect No. of Entries for Category Daily. MQ should have 288 entries.");
                    }
                }
            } else {
                retVal.setStatus(ACCEPTED);
                retVal.setErrorDetail("");
            }
        } else {
            retVal.setStatus(ACCEPTED);
            retVal.setErrorDetail("");
        }

        return retVal;
    }

}
