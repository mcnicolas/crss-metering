package com.pemc.crss.metering.parser.bcq;

import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.validator.BcqValidator;
import com.pemc.crss.metering.validator.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.supercsv.prefs.CsvPreference.STANDARD_PREFERENCE;

@Slf4j
@Component
public class BcqReader {

    private static final int DEFAULT_INTERVAL_CONFIG_IN_MINUTES = 5;

    public List<BcqHeader> readData(InputStream inputStream, Date validTradingDate)
            throws IOException, ValidationException {

        BcqValidator validator = new BcqValidator(DEFAULT_INTERVAL_CONFIG_IN_MINUTES, validTradingDate);

        List<List<String>> csv = new ArrayList<>();

        try (ICsvListReader reader = new CsvListReader(new InputStreamReader(inputStream), STANDARD_PREFERENCE)) {
            List<String> line;

            while ((line = reader.read()) != null) {
                csv.add(line);
            }
        }

        return validator.getAndValidateBcq(csv);
    }
}
