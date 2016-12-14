package com.pemc.crss.metering.parser.meterquantity;

import com.pemc.crss.metering.constants.FileType;
import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.parser.ParseException;
import com.pemc.crss.metering.parser.QuantityReader;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static com.pemc.crss.metering.constants.FileType.CSV;
import static com.pemc.crss.metering.constants.FileType.MDEF;
import static com.pemc.crss.metering.constants.FileType.XLS;

@Component
public class MeterQuantityParser {

    public MeterData parse(FileManifest fileManifest, byte[] fileContent) throws ParseException {
        try {
            QuantityReader reader = getMeterQuantityReader(fileManifest.getFileType());

            return reader.readData(fileManifest, new ByteArrayInputStream(fileContent));
        } catch (IOException e) {
            // TODO: Improve error message. If it is possible to capture culprit record
            throw new ParseException(e.getMessage(), e);
        }
    }

    private QuantityReader getMeterQuantityReader(FileType fileType) {
        QuantityReader retVal = null;

        if (fileType == MDEF) {
            retVal = new MeterQuantityMDEFReader();
        } else if (fileType == XLS) {
            retVal = new MeterQuantityExcelReader();
        } else if (fileType == CSV) {
            retVal = new MeterQuantityCSVReader();
        }

        return retVal;
    }

}
