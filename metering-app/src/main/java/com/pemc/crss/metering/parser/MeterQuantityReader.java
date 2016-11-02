package com.pemc.crss.metering.parser;

import com.pemc.crss.metering.dto.MeterData2;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface MeterQuantityReader {

    List<MeterData2> readData(InputStream inputStream) throws IOException;

}
