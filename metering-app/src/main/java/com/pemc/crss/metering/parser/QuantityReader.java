package com.pemc.crss.metering.parser;

import com.pemc.crss.metering.dto.mq.MeterData;

import java.io.IOException;
import java.io.InputStream;

public interface QuantityReader {

    MeterData readData(InputStream inputStream) throws IOException;

}
