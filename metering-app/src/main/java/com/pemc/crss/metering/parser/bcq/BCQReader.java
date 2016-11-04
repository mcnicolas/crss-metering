package com.pemc.crss.metering.parser.bcq;

import com.pemc.crss.metering.dto.BCQData;
import com.pemc.crss.metering.parser.QuantityReader;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BCQReader implements QuantityReader<BCQData> {

    @Override
    public List<BCQData> readData(InputStream inputStream) throws IOException {
        return new ArrayList<>();
    }
}
