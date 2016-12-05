package com.pemc.crss.metering.validator.mq;

import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import com.pemc.crss.metering.validator.ValidationResult;
import com.pemc.crss.metering.validator.Validator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.pemc.crss.metering.constants.UploadType.DAILY;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static com.pemc.crss.metering.utils.DateTimeUtils.isYesterday;
import static org.apache.commons.lang3.time.DateUtils.isSameDay;

@Component
@Order(value = 4)
public class OpenTradingDateValidator implements Validator {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public ValidationResult validate(FileManifest fileManifest, MeterData meterData) {
        ValidationResult retVal = new ValidationResult();

        if (fileManifest.getUploadType() == DAILY) {
            Date now = new Date();

            List<MeterDataDetail> meterDataDetails = meterData.getDetails();

            for (MeterDataDetail meterDataDetail : meterDataDetails) {
                Date readingDateTime = meterDataDetail.getReadingDateTime();

                if (!isSameDay(now, readingDateTime) && !isYesterday(now, readingDateTime)) {
                    retVal.setStatus(REJECTED);

                    String errorMessage = "Trading Date is Not Allowed. Submission of Daily MQ is closed for "
                            + dateFormat.format(readingDateTime);
                    retVal.setErrorDetail(errorMessage);

                    break;
                }
            }
        } else {
            retVal.setStatus(ACCEPTED);
            retVal.setErrorDetail("");
        }

        return retVal;
    }

}
