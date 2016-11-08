package com.pemc.crss.metering.parser;

import com.pemc.crss.metering.dto.BCQData;
import com.pemc.crss.metering.parser.bcq.BCQReader;
import com.pemc.crss.metering.validator.exception.ValidationException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

//TODO Change to spock
@Ignore
public class BCQReaderTest {

    @Test
    public void parseWithHourlyInterval() throws IOException, ValidationException {
        BCQReader reader = new BCQReader();
        List<BCQData> dataList = reader.readData(new FileInputStream(
                new File(BCQReaderTest.class.getClassLoader().getResource(
                        "bcq/sample_bcq_file_hourly.csv").getFile())));

        assertThat(dataList.size(), is(equalTo(36)));
    }

    @Test
    public void parseWithHourlyIntervalAndMultipleBuyers() throws IOException, ValidationException {
        BCQReader reader = new BCQReader();
        List<BCQData> dataList = reader.readData(new FileInputStream(
                new File(BCQReaderTest.class.getClassLoader().getResource(
                        "bcq/sample_bcq_file_hourly_with_multiple_buyers.csv").getFile())));

        assertThat(dataList.size(), is(equalTo(48)));
    }

    @Test
    public void parseWithQuarterlyInterval() throws IOException, ValidationException {
        BCQReader reader = new BCQReader();
        List<BCQData> dataList = reader.readData(new FileInputStream(
                new File(BCQReaderTest.class.getClassLoader().getResource(
                        "bcq/sample_bcq_file_15mins.csv").getFile())));

        assertThat(dataList.size(), is(equalTo(9)));
    }

    @Test
    public void parseWith5MinuteInterval() throws IOException, ValidationException {
        BCQReader reader = new BCQReader();
        List<BCQData> dataList = reader.readData(new FileInputStream(
                new File(BCQReaderTest.class.getClassLoader().getResource(
                        "bcq/sample_bcq_file_5mins.csv").getFile())));

        assertThat(dataList.size(), is(equalTo(3)));
    }
}
