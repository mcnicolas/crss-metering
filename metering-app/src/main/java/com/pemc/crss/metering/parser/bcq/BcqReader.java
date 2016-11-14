package com.pemc.crss.metering.parser.bcq;

import com.pemc.crss.metering.dto.BcqData;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.validator.BcqCsvValidator;
import com.pemc.crss.metering.validator.BcqDataValidator;
import com.pemc.crss.metering.validator.exception.ValidationException;
import com.pemc.crss.metering.validator.util.BcqErrorMessageFormatter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static com.pemc.crss.metering.constants.BcqValidationMessage.DUPLICATE;
import static org.supercsv.prefs.CsvPreference.STANDARD_PREFERENCE;

@Slf4j
@Component
public class BcqReader {

    private static final int DEFAULT_INTERVAL_CONFIG_IN_MINUTES = 5;

    public Map<BcqHeader, List<BcqData>> readData(InputStream inputStream, Date validDeclarationDate)
            throws IOException, ValidationException {

        List<List<String>> dataRecord = new ArrayList<>();
        Set<List<String>> uniqueDataRecord = new LinkedHashSet<>(); //seller buyer date


        try (ICsvListReader reader = new CsvListReader(new InputStreamReader(inputStream), STANDARD_PREFERENCE)) {
            List<String> line;

            while ((line = reader.read()) != null) {

                if (reader.getLineNumber() > 2) {
                    List<String> data = new ArrayList<>();

                    data.add(line.get(0));
                    data.add(line.get(1));
                    data.add(line.get(3));

                    if (!uniqueDataRecord.add(data)) {
                        int lineNo = reader.getLineNumber();

                        throw new ValidationException(
                                BcqErrorMessageFormatter.formatMessage(lineNo, DUPLICATE, StringUtils.join(line, ", ")));
                    }
                }

                dataRecord.add(line);
            }

            BcqCsvValidator.validateCsv(dataRecord);
        }

        return BcqDataValidator.getAndValidateRecord(dataRecord,
                DEFAULT_INTERVAL_CONFIG_IN_MINUTES, validDeclarationDate);
    }
}
