package com.pemc.crss.metering.parser;

import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.parser.meterquantity.MeterQuantityExcelReader;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

// TODO: Convert to spock
public class MeterQuantityExcelReaderTest {

    @Test
    public void shouldParseXLS() throws FileNotFoundException, ParseException {
        QuantityReader reader = new MeterQuantityExcelReader();
        FileManifest fileManifest = new FileManifest();
        MeterData meterData = reader.readData(fileManifest,
                this.getClass().getClassLoader().getResourceAsStream(
                        "meterdata/xls/MF3MABAMSUZ01.xls"));

        assertThat(meterData.getDetails().size(), is(equalTo(2976)));
    }

    @Test
    public void shouldParseXLSX() throws IOException, ParseException {
        QuantityReader reader = new MeterQuantityExcelReader();
        FileManifest fileManifest = new FileManifest();
        MeterData meterData = reader.readData(fileManifest,
                this.getClass().getClassLoader().getResourceAsStream(
                        "meterdata/xls/MF3MABAMSUZ01.xls"));

        assertThat(meterData.getDetails().size(), is(equalTo(2976)));
    }

}
