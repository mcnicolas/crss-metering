package com.pemc.crss.metering.validator.mq;

import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.parser.QuantityReader;
import com.pemc.crss.metering.parser.meterquantity.MeterQuantityCSVReader;
import com.pemc.crss.metering.validator.ValidationResult;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * 1. Validate comma separated values
 *    - Happy path
 *    - Validate for required
 *    - Validate for optional values
 *    - Validate for abnormal data
 *
 * 2. Parse excel exported csv
 * 3. Parse tab separated values
 * 4. Parse semi colon separated values
 */

// TODO: Convert to spock
public class CSVColumnHeaderValidatorTest {

    @Test
    public void shouldValidateCorrectFormatCSV() throws IOException {
        // given
        FileManifest fileManifest = new FileManifest();

        QuantityReader reader = new MeterQuantityCSVReader();
        MeterData meterData = reader.readData(fileManifest, new FileInputStream(
                new File(CSVColumnHeaderValidatorTest.class.getClassLoader().getResource(
                        "meterdata/csv/correct_format_data.csv").getFile())));

        ColumnHeaderValidator validator = new ColumnHeaderValidator();

        // when
        ValidationResult validationResult = validator.validate(fileManifest, meterData);

        // then
        assertThat(validationResult.getStatus(), is(equalTo(ACCEPTED)));
    }

/*
    @Test
    public void shouldInvalidateNonCSVFile() throws IOException {
        // given
        FileManifest fileManifest = new FileManifest();

        QuantityReader reader = new MeterQuantityCSVReader();
        MeterData meterData = reader.readData(new FileInputStream(
                new File(CSVColumnHeaderValidatorTest.class.getClassLoader().getResource(
                        "meterdata/csv/invalid.csv").getFile())));

        ColumnHeaderValidator validator = new ColumnHeaderValidator();

        // when
        ValidationResult validationResult = validator.validate(fileManifest, meterData);

        // then
        assertThat(validationResult.getStatus(), is(equalTo(ACCEPTED)));
    }
*/

}
