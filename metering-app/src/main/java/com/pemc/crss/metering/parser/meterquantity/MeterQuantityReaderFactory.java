package com.pemc.crss.metering.parser.meterquantity;

import com.pemc.crss.metering.dto.MeterData2;
import com.pemc.crss.metering.parser.QuantityReader;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Component
public class MeterQuantityReaderFactory {

    public QuantityReader<MeterData2> getMeterQuantityReader(String fileType) {
        QuantityReader<MeterData2> retVal = null;

        if (equalsIgnoreCase(fileType, "MDEF")) {
            retVal = new MeterQuantityMDEFReader();
        } else if (equalsIgnoreCase(fileType, "XLS")) {
            retVal = new MeterQuantityExcelReader();
        } else if (equalsIgnoreCase(fileType, "CSV")) {
            retVal = new MeterQuantityCSVReader();
        }

        return retVal;
    }

}
