package com.pemc.crss.metering.parser.bcq;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface BcqReader {

    List<List<String>> readCsv(InputStream inputStream) throws IOException;

}
