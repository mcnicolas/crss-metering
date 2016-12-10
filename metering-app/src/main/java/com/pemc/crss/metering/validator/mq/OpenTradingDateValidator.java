package com.pemc.crss.metering.validator.mq;

import com.pemc.crss.metering.constants.FileType;
import com.pemc.crss.metering.constants.UploadType;
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

import static com.pemc.crss.metering.constants.FileType.CSV;
import static com.pemc.crss.metering.constants.FileType.XLS;
import static com.pemc.crss.metering.constants.UploadType.DAILY;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static com.pemc.crss.metering.utils.DateTimeUtils.isYesterday;
import static com.pemc.crss.metering.validator.ValidationResult.ACCEPTED_STATUS;
import static org.apache.commons.lang3.time.DateUtils.isSameDay;

@Component
@Order(value = 3)
public class OpenTradingDateValidator implements Validator {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public ValidationResult validate(FileManifest fileManifest, MeterData meterData) {

        ValidationResult retVal = validateNullDate(fileManifest, meterData);

        if (retVal.getStatus() == ACCEPTED) {
            retVal = validateOpenTradingDate(fileManifest, meterData);
        }

        return retVal;
    }

    private ValidationResult validateNullDate(FileManifest fileManifest, MeterData meterData) {
        ValidationResult retVal = ACCEPTED_STATUS;

        FileType fileType = fileManifest.getFileType();

        if (fileType == XLS || fileType == CSV) {
            List<MeterDataDetail> meterDataDetails = meterData.getDetails();

            for (MeterDataDetail meterDataDetail : meterDataDetails) {
                Date readingDateTime = meterDataDetail.getReadingDateTime();

                if (readingDateTime == null) {
                    retVal.setStatus(REJECTED);

                    String errorMessage = "Malformed trading date format. Should be YYYY-MM-DD HH:MM";
                    retVal.setErrorDetail(errorMessage);

                    break;
                }
            }
        }

        return retVal;
    }

    private ValidationResult validateOpenTradingDate(FileManifest fileManifest, MeterData meterData) {
        ValidationResult retVal = ACCEPTED_STATUS;

        UploadType uploadType = fileManifest.getUploadType();
        if (uploadType == DAILY) {
            Date now = new Date();

            List<MeterDataDetail> meterDataDetails = meterData.getDetails();

            for (MeterDataDetail meterDataDetail : meterDataDetails) {
                Date readingDateTime = meterDataDetail.getReadingDateTime();

                if (readingDateTime == null) {
                    retVal.setStatus(REJECTED);

                    String errorMessage = "Malformed trading date format. Should be YYYY-MM-DD HH:MM";
                    retVal.setErrorDetail(errorMessage);

                    break;
                }

                if (!isSameDay(now, readingDateTime) && !isYesterday(now, readingDateTime)) {
                    retVal.setStatus(REJECTED);

                    String errorMessage = "Trading Date is Not Allowed. Submission of Daily MQ is closed for "
                            + dateFormat.format(readingDateTime);
                    retVal.setErrorDetail(errorMessage);

                    break;
                }
            }
        }

        return retVal;
    }

}
