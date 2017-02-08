package com.pemc.crss.commons.reports;

import lombok.extern.slf4j.Slf4j;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

@Slf4j
public class ReportCsvWriter {

    public static <T extends AbstractReportBuilder> void write(T reportBuilder, OutputStream outputStream) {

        try (ICsvBeanWriter beanWriter = new CsvBeanWriter(new OutputStreamWriter(outputStream),
                CsvPreference.STANDARD_PREFERENCE)) {

            beanWriter.writeHeader(reportBuilder.getHeaders());

            for (final ReportBean row :reportBuilder.getReportBeans()) {
                beanWriter.write(row, reportBuilder.getFieldMapper(), reportBuilder.getProcessors());
            }

        } catch (IOException e) {
            log.error("There was an error writing the csv file: " + e.getMessage());
        }
    }
}
