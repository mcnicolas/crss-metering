package com.pemc.crss.metering.validator.mq;

import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.dto.mq.MeterDataHeader;
import com.pemc.crss.metering.validator.ValidationResult;
import com.pemc.crss.metering.validator.Validator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.pemc.crss.metering.constants.FileType.CSV;
import static com.pemc.crss.metering.constants.FileType.XLS;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.validator.ValidationResult.ACCEPTED_STATUS;
import static com.pemc.crss.metering.validator.ValidationResult.REJECTED_STATUS;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Component
@Order(value = 2)
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
            "ESTIMATED_FLAG"
    };

    @Override
    public ValidationResult validate(FileManifest fileManifest, MeterData meterData) {
        ValidationResult retVal = new ValidationResult();

        if (fileManifest.getFileType() == XLS || fileManifest.getFileType() == CSV) {
            MeterDataHeader header = meterData.getHeader();

            retVal = checkRequiredColumns(header.getColumnNames());

            if (retVal.getStatus() == ACCEPTED) {
                retVal = checkOptionalColumns(header.getColumnNames());
            }

            if (retVal.getStatus() != ACCEPTED) {
                retVal.setErrorDetail("Incorrect Header Names. Column Header Names and Order should be as follows:"
                        +  " SEIL, BDATE, TIME, KW_DEL, KWH_DEL, KVARH_DEL, KW_REC, KWH_REC, KVARH_REC, ESTIMATION_FLAG");
            }
        } else {
            retVal.setStatus(ACCEPTED);
            retVal.setErrorDetail("");
        }

        return retVal;
    }

    // TODO: Optimize code structure
    private ValidationResult checkRequiredColumns(List<String> columnNames) {
        ValidationResult retVal = null;

        for (int i = 0; i < 3; i++) {
            if (!equalsIgnoreCase(columnNames.get(i), COLUMNS[i])) {
                retVal = REJECTED_STATUS;
                break;
            }
        }

        if (retVal == null) {
            retVal = ACCEPTED_STATUS;
        }

        return retVal;
    }

    // TODO: Optimize code structure
    private ValidationResult checkOptionalColumns(List<String> columnNames) {
        ValidationResult retVal = null;

        for (int i = 3; i < columnNames.size(); i++) {
            if (!equalsIgnoreCase(columnNames.get(i), COLUMNS[i])) {
                retVal = REJECTED_STATUS;
                break;
            }
        }

        if (retVal == null) {
            retVal = ACCEPTED_STATUS;
        }

        return retVal;
    }

}
