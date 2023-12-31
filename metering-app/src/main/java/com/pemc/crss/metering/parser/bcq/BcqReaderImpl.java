package com.pemc.crss.metering.parser.bcq;

import org.springframework.stereotype.Component;
import org.supercsv.exception.SuperCsvException;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.supercsv.prefs.CsvPreference.STANDARD_PREFERENCE;

@Component
public class BcqReaderImpl implements BcqReader {

    public List<List<String>> readCsv(InputStream inputStream) throws IOException {
        List<List<String>> csv = new ArrayList<>();
        try (ICsvListReader reader = new CsvListReader(new InputStreamReader(inputStream), STANDARD_PREFERENCE)) {
            List<String> line;
            int lastLineNumber = 0;
            while ((line = reader.read()) != null) {
                if (reader.getLineNumber() - lastLineNumber != 1) {
                    csv.add(asList("", "", "", "", "",""));
                }
                csv.add(line);
                lastLineNumber = reader.getLineNumber();
            }
        } catch (SuperCsvException ex) {
            throw new IOException(ex);
        }

        return csv;
    }

}
