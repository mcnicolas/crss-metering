package com.pemc.crss.metering.validator.bcq.helper;

import com.pemc.crss.metering.validator.bcq.validation.Validation;

import java.util.List;

public interface CsvValidationHelper {

    Validation<List<List<String>>> validCsv(int intervalConfig);

}
