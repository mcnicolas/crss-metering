package com.pemc.crss.metering.parser;

import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;

import java.io.IOException;
import java.io.InputStream;
import java.text.*;

public interface QuantityReader {

    MeterData readData(FileManifest fileManifest, InputStream inputStream) throws ParseException;

}
