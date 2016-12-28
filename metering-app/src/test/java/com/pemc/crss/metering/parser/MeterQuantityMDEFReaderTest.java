package com.pemc.crss.metering.parser;

import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.parser.meterquantity.MeterQuantityMDEFReader;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

// TODO: Convert to spock
public class MeterQuantityMDEFReaderTest {

    @Test
    public void parseNormalMDEFFile() throws IOException, ParseException, URISyntaxException {
        // given
        String[][] files = new String[][] {
                {"normal_mdef_01.mde", "1090"},
                {"normal_mdef_02.mde", "168"},
                {"normal_mdef_03.mde", "69"},
                {"normal_mdef_04.mde", "117"}
        };

        QuantityReader meterReader = new MeterQuantityMDEFReader();
        FileManifest fileManifest = new FileManifest();

        for (String[] file : files) {
            String fileName = "/meterdata/parsing/mdef/" + file[0];
            Path path = Paths.get(getClass().getResource(fileName).toURI());
            InputStream inputStream = Files.newInputStream(path);

            // when
            MeterData meterData = meterReader.readData(fileManifest, inputStream);

            // then
            assertThat(meterData.getDetails().size(), is(equalTo(Integer.parseInt(file[1]))));
        }
    }

    @Test
    public void parseMDEFWithUnknownUOM() throws URISyntaxException, IOException, ParseException {
        QuantityReader meterReader = new MeterQuantityMDEFReader();
        FileManifest fileManifest = new FileManifest();

        String fileName = "/meterdata/parsing/mdef/unknown_uom.mde";
        Path path = Paths.get(getClass().getResource(fileName).toURI());
        InputStream inputStream = Files.newInputStream(path);

        MeterData meterData = meterReader.readData(fileManifest, inputStream);

        assertThat(meterData.getDetails().size(), is(equalTo(97)));
    }

}
