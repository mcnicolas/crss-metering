package com.pemc.crss.metering.parser;

import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Component
public class MeterQuantityReaderFactory {

    public MeterQuantityReader getMeterQuantityReader(String fileType) {
        MeterQuantityReader retVal = null;

        if (equalsIgnoreCase(fileType, "MDEF")) {
            retVal = new MDEFReader();
        } else if (equalsIgnoreCase(fileType, "XLS")) {
            retVal = new ExcelReader();
        } else if (equalsIgnoreCase(fileType, "CSV")) {
            retVal = new CSVReader();
        }

        return retVal;
    }

}
