package com.pemc.crss.metering.parser.meterquantity;

import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.parser.QuantityReader;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

// TODO: Convert to spock
public class MeterQuantityMDEFReaderTest {

    @Test
    public void shouldParseMDEF() throws IOException, ParseException {
        QuantityReader meterReader = new MeterQuantityMDEFReader();

        FileManifest fileManifest = new FileManifest();
        MeterData meterData = meterReader.readData(
                fileManifest, new FileInputStream(
                        new File(MeterQuantityMDEFReaderTest.class.getClassLoader().getResource(
                                "meterdata/daily/Luzon/DT030365.MDE").getFile())));

        assertThat(meterData.getDetails().size(), is(equalTo(1090)));
    }

}
