package com.pemc.crss.metering.parser.meterquantity;

import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.parser.QuantityReader;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

// TODO: Convert to spock
public class MeterQuantityMDEFReaderTest {

    @Test
    public void shouldParseMDEF() throws IOException {
        QuantityReader meterReader = new MeterQuantityMDEFReader();

        MeterData meterData = meterReader.readData(
                new FileInputStream(
                        new File(MeterQuantityMDEFReaderTest.class.getClassLoader().getResource(
                                "meterdata/daily/Luzon/DT030365.MDE").getFile())));

        assertThat(meterData.getMeterDataDetails().size(), is(equalTo(1090)));
    }

}
