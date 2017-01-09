package com.pemc.crss.metering.validator.mq;

import com.pemc.crss.metering.constants.UploadType;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.parser.ParseException;
import com.pemc.crss.metering.parser.QuantityReader;
import com.pemc.crss.metering.parser.meterquantity.MeterQuantityExcelReader;
import com.pemc.crss.metering.validator.ValidationResult;
import com.pemc.crss.metering.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.pemc.crss.metering.constants.FileType.XLS;
import static com.pemc.crss.metering.constants.UploadType.DAILY;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

// TODO: Convert to spock
@Slf4j
public class CorrectIntervalValidatorTest {

    @Test
    public void correctInterval15Minutes() throws FileNotFoundException, ParseException {
        // given
        String excelFile = "meterdata/validation/interval/correct_interval_15mins.xls";
        FileManifest fileManifest = new FileManifest();
        fileManifest.setFileType(XLS);
        fileManifest.setUploadType(DAILY);
        fileManifest.setFileName(excelFile);

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(excelFile);

        QuantityReader reader = new MeterQuantityExcelReader();
        MeterData meterData = reader.readData(fileManifest, inputStream);

        Validator validator = new CorrectIntervalValidator(initializeCache());

        // when
        ValidationResult result = validator.validate(fileManifest, meterData);

        // then
        log.debug("Error Detail:{}", result.getErrorDetail());
        assertThat(result.getStatus(), is(equalTo(ACCEPTED)));
    }

    @Test
    public void incorrectInterval15Minutes() throws FileNotFoundException, ParseException {
        // given
        String excelFile = "meterdata/validation/interval/incorrect_interval_15mins.xls";
        FileManifest fileManifest = new FileManifest();
        fileManifest.setFileType(XLS);
        fileManifest.setUploadType(DAILY);
        fileManifest.setFileName(excelFile);

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(excelFile);

        QuantityReader reader = new MeterQuantityExcelReader();
        MeterData meterData = reader.readData(fileManifest, inputStream);

        Validator validator = new CorrectIntervalValidator(initializeCache());

        // when
        ValidationResult result = validator.validate(fileManifest, meterData);

        // then
        log.debug("Error Detail:{}", result.getErrorDetail());
        assertThat(result.getStatus(), is(equalTo(REJECTED)));
    }

    private SimpleCacheManager initializeCache() {
        Cache cache = new ConcurrentMapCache("config");
        cache.put("MQ_INTERVAL", "15");

        List<Cache> cacheList = new ArrayList<>();
        cacheList.add(cache);

        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(cacheList);
        cacheManager.initializeCaches();
        return cacheManager;
    }

}
