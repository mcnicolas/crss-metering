package com.pemc.crss.metering.parser.bcq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.supercsv.prefs.CsvPreference.STANDARD_PREFERENCE;

@Slf4j
@Component
public class BcqReader {

    public List<List<String>> readCsv(InputStream inputStream) throws IOException {
        List<List<String>> csv = new ArrayList<>();
        try (ICsvListReader reader = new CsvListReader(new InputStreamReader(inputStream), STANDARD_PREFERENCE)) {
            List<String> line;
            while ((line = reader.read()) != null) {
                csv.add(line);
            }
        }
        return csv;
    }

}
