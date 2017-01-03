package com.pemc.crss.metering.validator.mq;

import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.parser.ParseException;
import com.pemc.crss.metering.parser.QuantityReader;
import com.pemc.crss.metering.parser.meterquantity.MeterQuantityExcelReader;
import com.pemc.crss.metering.validator.ValidationResult;
import com.pemc.crss.metering.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import static com.pemc.crss.metering.constants.FileType.XLS;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

@Slf4j
// TODO: Seems to have a synchronization issue. Need to revisit.
@FixMethodOrder(NAME_ASCENDING)
public class DuplicateDataValidatorTest {

    @Test
    public void noDuplicates() throws ParseException, IOException, URISyntaxException {
        // given
        String excelFile = "meterdata/validation/duplicate/no_duplicates.xls";
        FileManifest fileManifest = new FileManifest();
        fileManifest.setFileType(XLS);
        fileManifest.setFileName(excelFile);

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(excelFile);

        QuantityReader reader = new MeterQuantityExcelReader();
        MeterData meterData = reader.readData(fileManifest, inputStream);

        Validator validator = new DuplicateDataValidator();

        // when
        ValidationResult result = validator.validate(fileManifest, meterData);

        // then
        log.debug("Error Detail:{}", result.getErrorDetail());
        assertThat(result.getStatus(), is(equalTo(ACCEPTED)));
    }

    @Test
    public void withDuplicates() throws ParseException, IOException, URISyntaxException {
        // given
        String excelFile = "meterdata/validation/duplicate/with_duplicates.xls";
        FileManifest fileManifest = new FileManifest();
        fileManifest.setFileType(XLS);
        fileManifest.setFileName(excelFile);

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(excelFile);

        QuantityReader reader = new MeterQuantityExcelReader();
        MeterData meterData = reader.readData(fileManifest, inputStream);

        Validator validator = new DuplicateDataValidator();

        // when
        ValidationResult result = validator.validate(fileManifest, meterData);

        // then
        log.debug("Error Detail:{}", result.getErrorDetail());
        assertThat(result.getStatus(), is(equalTo(REJECTED)));
    }

}
