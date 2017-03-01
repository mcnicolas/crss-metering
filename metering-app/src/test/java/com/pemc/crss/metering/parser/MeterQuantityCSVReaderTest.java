package com.pemc.crss.metering.parser;

import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.parser.meterquantity.MeterQuantityCSVReader;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@Slf4j
public class MeterQuantityCSVReaderTest {

    @Test
    public void parseCSVWithMissingColumns() throws FileNotFoundException, ParseException {
        // given
        String[][] paths = new String[][] {
                {"meterdata/csv/MF3MABAMSUZ01.csv", "2976"},
                {"meterdata/csv/MF3MARAMECO01.csv", "8640"}
        };

        QuantityReader reader = new MeterQuantityCSVReader();
        FileManifest fileManifest = new FileManifest();

        for (String[] path : paths) {
            log.debug("Processing: {}", path[0]);

            // when
            MeterData meterData = reader.readData(fileManifest, getInputStream(path[0]));

            // then
            assertThat(meterData.getDetails().size(), is(equalTo(Integer.parseInt(path[1]))));
        }
    }

    @Test
    public void parseCSVWithExtraColumns() throws FileNotFoundException, ParseException {
        QuantityReader reader = new MeterQuantityCSVReader();
        FileManifest fileManifest = new FileManifest();
        MeterData meterData = reader.readData(fileManifest,
                getInputStream("meterdata/csv/MET_CEDCMSP_R3MEXCEDC01TNSC01_20161003.csv"));

        assertThat(meterData.getDetails().size(), is(equalTo(96)));
    }

    private InputStream getInputStream(String filename) throws FileNotFoundException {
        return new FileInputStream(
                new File(MeterQuantityCSVReaderTest.class.getClassLoader().getResource(
                        filename).getFile()));
    }

}
