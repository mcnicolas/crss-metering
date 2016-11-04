package com.pemc.crss.metering.parser;

import com.pemc.crss.metering.dto.MeterData2;
import com.pemc.crss.metering.parser.meterquantity.MeterQuantityExcelReader;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

// TODO: Convert to spock
public class MeterQuantityExcelReaderTest {

    @Test
    public void shouldParseXLS() throws IOException {
        QuantityReader<MeterData2> reader = new MeterQuantityExcelReader();
        List<MeterData2> meterData = reader.readData(new FileInputStream(
                new File(MeterQuantityExcelReaderTest.class.getClassLoader().getResource(
                        "meterdata/xls/MF3MABAMSUZ01.xls").getFile())));

        assertThat(meterData.size(), is(equalTo(2976)));
    }

    @Test
    public void shouldParseXLSX() throws IOException {
        QuantityReader<MeterData2> reader = new MeterQuantityExcelReader();
        List<MeterData2> meterData = reader.readData(new FileInputStream(
                new File(MeterQuantityExcelReaderTest.class.getClassLoader().getResource(
                        "meterdata/xls/MF3MABAMSUZ01.xls").getFile())));

        assertThat(meterData.size(), is(equalTo(2976)));
    }

}
