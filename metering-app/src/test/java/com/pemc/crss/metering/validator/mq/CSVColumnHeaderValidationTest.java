package com.pemc.crss.metering.validator.mq;

import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.parser.ParseException;
import com.pemc.crss.metering.parser.QuantityReader;
import com.pemc.crss.metering.parser.meterquantity.MeterQuantityCSVReader;
import com.pemc.crss.metering.validator.ValidationResult;
import com.pemc.crss.metering.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.pemc.crss.metering.constants.FileType.CSV;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

// TODO: Convert to Spock
@Slf4j
public class CSVColumnHeaderValidationTest {

    @Test
    public void validateCompleteHeaders() throws ParseException, IOException, URISyntaxException {
        // given
        String excelFile = "/meterdata/validation/header/complete_headers.csv";
        MeterData meterData = readMeterData(excelFile);

        FileManifest fileManifest = new FileManifest();
        fileManifest.setFileType(CSV);
        fileManifest.setFileName(excelFile);

        Validator validator = new ColumnHeaderValidator();

        // when
        ValidationResult result = validator.validate(fileManifest, meterData);

        // then
        log.debug(result.getErrorDetail());
        assertThat(result.getStatus(), is(equalTo(ACCEPTED)));
    }

    @Test
    public void validateRequiredColumns() throws ParseException, IOException, URISyntaxException {
        // given
        String excelFile = "/meterdata/validation/header/minimal_columns.csv";
        MeterData meterData = readMeterData(excelFile);

        FileManifest fileManifest = new FileManifest();
        fileManifest.setFileType(CSV);
        fileManifest.setFileName(excelFile);

        Validator validator = new ColumnHeaderValidator();

        // when
        ValidationResult result = validator.validate(fileManifest, meterData);

        // then
        log.debug(result.getErrorDetail());
        assertThat(result.getStatus(), is(equalTo(ACCEPTED)));
    }

    @Test
    public void validateWrongColumnNames() throws ParseException, IOException, URISyntaxException {
        // given
        String excelFile = "/meterdata/validation/header/wrong_column_names.csv";
        MeterData meterData = readMeterData(excelFile);

        FileManifest fileManifest = new FileManifest();
        fileManifest.setFileType(CSV);
        fileManifest.setFileName(excelFile);

        Validator validator = new ColumnHeaderValidator();

        // when
        ValidationResult result = validator.validate(fileManifest, meterData);

        // then
        log.debug(result.getErrorDetail());
        assertThat(result.getStatus(), is(equalTo(REJECTED)));
    }

    @Ignore
    @Test
    public void validateMissingColumns() throws ParseException, IOException, URISyntaxException {
        // given
        String excelFile = "/meterdata/validation/header/missing_columns.csv";
        MeterData meterData = readMeterData(excelFile);

        FileManifest fileManifest = new FileManifest();
        fileManifest.setFileType(CSV);
        fileManifest.setFileName(excelFile);

        Validator validator = new ColumnHeaderValidator();

        // when
        ValidationResult result = validator.validate(fileManifest, meterData);

        // then
        log.debug(result.getErrorDetail());
        assertThat(result.getStatus(), is(equalTo(REJECTED)));
    }

    private MeterData readMeterData(String excelFile) throws URISyntaxException, IOException, ParseException {
        QuantityReader reader = new MeterQuantityCSVReader();
        FileManifest fileManifest = new FileManifest();

        Path path = Paths.get(getClass().getResource(excelFile).toURI());
        InputStream inputStream = Files.newInputStream(path);

        return reader.readData(fileManifest, inputStream);
    }

}
