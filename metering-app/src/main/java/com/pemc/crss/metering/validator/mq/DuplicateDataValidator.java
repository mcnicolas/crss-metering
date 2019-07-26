package com.pemc.crss.metering.validator.mq;

import com.pemc.crss.metering.constants.FileType;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import com.pemc.crss.metering.validator.ValidationResult;
import com.pemc.crss.metering.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.pemc.crss.metering.constants.FileType.CSV;
import static com.pemc.crss.metering.constants.FileType.XLS;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;

@Slf4j
@Component
@Order(value = 5)
public class DuplicateDataValidator implements Validator {

    @Override
    public ValidationResult validate(FileManifest fileManifest, MeterData meterData) {
        ValidationResult retVal = new ValidationResult();
        retVal.setStatus(ACCEPTED);

        FileType fileType = fileManifest.getFileType();
        if (fileType == XLS || fileType == CSV) {
            SortedSet<String> interval = new TreeSet<>();

            List<MeterDataDetail> meterDetails = meterData.getDetails();
            for (MeterDataDetail meterDataDetail : meterDetails) {
                String key = meterDataDetail.getSein() + "_" + String.valueOf(meterDataDetail.getReadingDateTime());
                boolean success = interval.add(key);

                if (!success) {
                    retVal.setStatus(REJECTED);
                    retVal.setErrorDetail("Duplicate time interval:" + key);

                    log.warn("Found duplicate record: SEIN:{} Reading Date/Time:{}",
                            meterDataDetail.getSein(), meterDataDetail.getReadingDateTime());

                    break;
                }
            }
        }

        return retVal;
    }

}
