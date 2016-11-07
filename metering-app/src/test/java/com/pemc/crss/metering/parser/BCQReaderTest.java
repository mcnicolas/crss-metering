package com.pemc.crss.metering.parser;

import com.pemc.crss.metering.dto.BCQData;
import com.pemc.crss.metering.parser.bcq.BCQReader;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BCQReaderTest {

    @Test
    public void parseWithHourlyInterval() throws IOException {
        BCQReader reader = new BCQReader();
        List<BCQData> dataList = reader.readData(new FileInputStream(
                new File(BCQReaderTest.class.getClassLoader().getResource(
                        "bcq/sample_bcq_file_hourly.csv").getFile())));

        assertThat(dataList.size(), is(equalTo(36)));
    }

    @Test
    public void parseWithQuarterlyInterval() throws IOException {
        BCQReader reader = new BCQReader();
        List<BCQData> dataList = reader.readData(new FileInputStream(
                new File(BCQReaderTest.class.getClassLoader().getResource(
                        "bcq/sample_bcq_file_15mins.csv").getFile())));

        assertThat(dataList.size(), is(equalTo(9)));
    }

    @Test
    public void parseWith5MinuteInterval() throws IOException {
        BCQReader reader = new BCQReader();
        List<BCQData> dataList = reader.readData(new FileInputStream(
                new File(BCQReaderTest.class.getClassLoader().getResource(
                        "bcq/sample_bcq_file_5mins.csv").getFile())));

        assertThat(dataList.size(), is(equalTo(3)));
    }
}
