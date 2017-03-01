package com.pemc.crss.metering.validator.bcq.helper

import com.pemc.crss.metering.parser.bcq.BcqReader

class ValidationHelperTestUtils {

    static def reader = new BcqReader()

    static def readCsv(String csvFileName) {
        if (csvFileName == null) {
            return null
        }
        def csvPath = 'bcq/validation/csv/'.concat(csvFileName).concat('.csv');
        def file = new File(CsvValidationHelperTest.class.getClassLoader().getResource(csvPath).getFile())

        return reader.readCsv(new FileInputStream(file))
    }

}