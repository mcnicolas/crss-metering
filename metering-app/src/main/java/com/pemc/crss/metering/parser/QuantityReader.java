package com.pemc.crss.metering.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface QuantityReader<T> {

    List<T> readData(InputStream inputStream) throws IOException;

}
