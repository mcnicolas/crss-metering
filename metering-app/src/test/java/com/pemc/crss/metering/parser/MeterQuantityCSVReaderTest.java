package com.pemc.crss.metering.parser;

import com.pemc.crss.metering.dto.MeterData2;
import com.pemc.crss.metering.parser.meterquantity.MeterQuantityCSVReader;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

// TODO: Convert to spock
public class MeterQuantityCSVReaderTest {

    @Test
    public void parseCSVWithMissingColumns() throws IOException {
        QuantityReader<MeterData2> reader = new MeterQuantityCSVReader();
        List<MeterData2> meterData = reader.readData(new FileInputStream(
                new File(MeterQuantityCSVReaderTest.class.getClassLoader().getResource(
                        "meterdata/csv/MF3MABAMSUZ01.csv").getFile())));

        assertThat(meterData.size(), is(equalTo(2976)));
    }

    @Test
    public void parseCSVWithExtraColumns() throws IOException {
        QuantityReader<MeterData2> reader = new MeterQuantityCSVReader();
        List<MeterData2> meterData = reader.readData(new FileInputStream(
                new File(MeterQuantityCSVReaderTest.class.getClassLoader().getResource(
                        "meterdata/csv/MET_CEDCMSP_R3MEXCEDC01TNSC01_20161003.csv").getFile())));

        assertThat(meterData.size(), is(equalTo(96)));
    }

}
