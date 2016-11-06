package com.pemc.crss.metering.parser.meterquantity;

import com.pemc.crss.metering.dto.MeterData2;
import com.pemc.crss.metering.parser.QuantityReader;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

// TODO: Convert to spock
public class MeterQuantityMDEFReaderTest {

    @Test
    public void shouldParseMDEF() throws IOException {
        QuantityReader<MeterData2> meterReader = new MeterQuantityMDEFReader();

        List<MeterData2> meterData = meterReader.readData(
                new FileInputStream(
                        new File(MeterQuantityMDEFReaderTest.class.getClassLoader().getResource("meterdata/daily/Luzon/DT030365.MDE").getFile()))
        );

        assertThat(meterData.size(), is(equalTo(1090)));
    }

}
