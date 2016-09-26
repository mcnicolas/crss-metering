package com.pemc.crss.metering.parser;

import org.junit.Test;

import java.io.File;

public class MDEFReaderTest {

    @Test
    public void shouldReadMDEFFile() throws Exception {
        MDEFReader reader = new MDEFReader();
        MeterData meterData = reader.readMDEF(new File(MDEFReaderTest.class.getClassLoader().getResource("DT030188.MDE").getFile()));
        System.out.println("Hello world");
    }

}