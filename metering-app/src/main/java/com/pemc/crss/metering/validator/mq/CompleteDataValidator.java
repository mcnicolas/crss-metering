package com.pemc.crss.metering.validator.mq;

import com.pemc.crss.metering.constants.FileType;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.validator.ValidationResult;
import com.pemc.crss.metering.validator.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static com.pemc.crss.metering.constants.FileType.CSV;
import static com.pemc.crss.metering.constants.FileType.XLS;
import static com.pemc.crss.metering.constants.UploadType.DAILY;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;

@Component
@Order(value = 2)
public class CompleteDataValidator implements Validator {

    private static final int DEFAULT_INTERVAL = 15;

    private final CacheManager cacheManager;

    @Autowired
    public CompleteDataValidator(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public ValidationResult validate(FileManifest fileManifest, MeterData meterData) {
        ValidationResult retVal = new ValidationResult();

        FileType fileType = fileManifest.getFileType();
        if ((fileType == XLS || fileType == CSV)
                && fileManifest.getUploadType() == DAILY) {

            Cache cache = cacheManager.getCache("config");

            int interval = DEFAULT_INTERVAL;
            String intervalObj = cache.get("MQ_INTERVAL", String.class);
            if (intervalObj != null) {
                interval = Integer.parseInt(intervalObj);
            }

            int dataSize = meterData.getDetails().size();
            if (interval == 15 && dataSize != 96) {
                retVal.setStatus(REJECTED);
                retVal.setErrorDetail("Incomplete Daily MQ. MQ should have 96 entries.");
            } else if (interval == 5 && dataSize != 288) {
                retVal.setStatus(REJECTED);
                retVal.setErrorDetail("Incomplete Daily MQ. MQ should have 288 entries.");
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
