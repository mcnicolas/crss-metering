package com.pemc.crss.metering.parser;

import com.pemc.crss.metering.dto.MeterDataCSV;
import org.springframework.stereotype.Component;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.Trim;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCsvReflectionException;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.util.CsvContext;
import org.supercsv.util.MethodCache;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.supercsv.prefs.CsvPreference.EXCEL_PREFERENCE;

@Component
public class CSVReader {

    private final MethodCache cache = new MethodCache();

    public List<MeterDataCSV> readCSV(InputStream inputStream) throws IOException {
        List<MeterDataCSV> meterData = new ArrayList<>();

        try (ICsvListReader reader = new CsvListReader(new InputStreamReader(inputStream), EXCEL_PREFERENCE)) {

            reader.getHeader(true);
            String[] headers = getHeaders();
            CellProcessor[] processors = getProcessors();

            List<String> row;
            while ((row = reader.read()) != null) {
                meterData.add(populateBean(headers, processors, reader.getRowNumber(), reader.getLineNumber(), row));
            }
        }

        return meterData;
    }

    private MeterDataCSV populateBean(String[] headers, CellProcessor[] processors, int rowNumber, int lineNumber, List<String> row) {
        MeterDataCSV retVal = instantiateBean(MeterDataCSV.class);

        for (int i = 0; i < headers.length; i++) {
            CsvContext context = new CsvContext(lineNumber, rowNumber, i);
            Object fieldValue = processors[i].execute(row.get(i), context);

            Method setMethod = cache.getSetMethod(retVal, headers[i], fieldValue.getClass());
            invokeSetter(retVal, setMethod, fieldValue);
        }

        return retVal;
    }

    private static void invokeSetter(final Object bean, final Method setMethod, final Object fieldValue) {
        try {
            setMethod.invoke(bean, fieldValue);
        }
        catch(final Exception e) {
            throw new SuperCsvReflectionException(String.format("error invoking method %s()", setMethod.getName()), e);
        }
    }

    private <T> T instantiateBean(final Class<T> clazz) {
        T bean;

        try {
            bean = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new SuperCsvReflectionException(String.format(
                    "error instantiating bean, check that %s has a default no-args constructor", clazz.getName()), e);
        } catch (IllegalAccessException e) {
            throw new SuperCsvReflectionException("error instantiating bean", e);
        }

        return bean;
    }

    private String[] getHeaders() {
        return new String[]{
                "sein",
                "readingDate",
                "readingTime",
                "kwd",
                "kwhd",
                "kvarhd",
                "kwr",
                "kwhr",
                "kvarhr"
        };
    }

    private CellProcessor[] getProcessors() {
        return new CellProcessor[]{
                new Trim(),
                new Trim(),
                new Trim(),
                new ParseDouble(),
                new ParseDouble(),
                new ParseDouble(),
                new ParseDouble(),
                new ParseDouble(),
                new ParseDouble()
        };
    }

}
