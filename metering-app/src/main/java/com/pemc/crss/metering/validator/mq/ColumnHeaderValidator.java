package com.pemc.crss.metering.validator.mq;

import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.dto.mq.MeterDataHeader;
import com.pemc.crss.metering.validator.ValidationResult;
import com.pemc.crss.metering.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.pemc.crss.metering.constants.FileType.CSV;
import static com.pemc.crss.metering.constants.FileType.XLS;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@Order(value = 1)
public class ColumnHeaderValidator implements Validator {

    private static final String[] COLUMNS = {
            "SEIL",
            "BDATE",
            "TIME",
            "KW_DEL",
            "KWH_DEL",
            "KVARH_DEL",
            "KW_REC",
            "KWH_REC",
            "KVARH_REC",
            "ESTIMATION_FLAG"
    };

    @Override
    public ValidationResult validate(FileManifest fileManifest, MeterData meterData) {
        ValidationResult retVal = new ValidationResult();
        retVal.setStatus(ACCEPTED);

        if (fileManifest.getFileType() == XLS || fileManifest.getFileType() == CSV) {
            log.debug("Validating header for {}", fileManifest.getFileName());

            MeterDataHeader header = meterData.getHeader();

            retVal = checkRequiredColumns(header.getColumnNames());

            if (retVal.getStatus() == ACCEPTED) {
                retVal = checkOptionalColumns(header.getColumnNames());
            }

            if (retVal.getStatus() != ACCEPTED) {
                retVal.setErrorDetail("Incorrect Header Names."
                        + " Expected: SEIL, BDATE, TIME, KW_DEL, KWH_DEL, KVARH_DEL, KW_REC, KWH_REC, KVARH_REC, ESTIMATION_FLAG;"
                        + " Actual: " + String.join(", ", header.getColumnNames()));
            }
        }

        return retVal;
    }

    // TODO: Optimize code structure
    private ValidationResult checkRequiredColumns(List<String> columnNames) {
        ValidationResult retVal = new ValidationResult();
        retVal.setStatus(ACCEPTED);

        for (int i = 0; i < 5; i++) {
            if (!equalsIgnoreCase(columnNames.get(i), COLUMNS[i])) {
                retVal.setStatus(REJECTED);
                break;
            }
        }

        return retVal;
    }

    // TODO: Optimize code structure
    private ValidationResult checkOptionalColumns(List<String> columnNames) {
        ValidationResult retVal = new ValidationResult();
        retVal.setStatus(ACCEPTED);

        int maxIndex = COLUMNS.length;
        if (columnNames.size() < maxIndex) {
            maxIndex = columnNames.size();
        }

        for (int i = 5; i < maxIndex; i++) {
            String column = columnNames.get(i);

            if (isNotBlank(column) && !equalsIgnoreCase(columnNames.get(i), COLUMNS[i])) {
                retVal.setStatus(REJECTED);
                break;
            }
        }

        return retVal;
    }

}
