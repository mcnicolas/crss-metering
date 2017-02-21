package com.pemc.crss.metering.validator.bcq.helper

import com.pemc.crss.metering.parser.bcq.BcqReader
import spock.lang.Specification
import spock.lang.Unroll

import static com.pemc.crss.metering.constants.BcqValidationError.*
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED

class CsvValidationHelperTest extends Specification {

    def reader = new BcqReader()
    def CsvValidationHelper validationHelper = new CsvValidationHelper()

    @Unroll
    def 'must reject csv file with validation error: #validationError'() {
        given:
        def intervalConfig = 15

        when:
        def result = validationHelper.validCsv(intervalConfig).test(readCsv(csv))

        then:
        result.status == REJECTED
        result.errorMessage.validationError == validationError

        where:
        csv                                      || validationError
        null                                     || INVALID_CSV_FILE
        'bcq_file_empty'                         || EMPTY
        'bcq_file_invalid_header_count'          || INCORRECT_COLUMN_HEADER_COUNT
        'bcq_file_empty_line'                    || EMPTY_LINE
        'bcq_file_no_interval'                   || MISSING_INTERVAL
        'bcq_file_invalid_interval'              || INCORRECT_DECLARED_INTERVAL
        'bcq_file_no_selling_mtn'                || MISSING_SELLING_MTN
        'bcq_file_no_billing_id'                 || MISSING_BILLING_ID
        'bcq_file_no_reference_mtn'              || MISSING_REFERENCE_MTN
        'bcq_file_no_date'                       || MISSING_DATE
        'bcq_file_no_bcq'                        || MISSING_BCQ
        'bcq_file_invalid_bcq'                   || INCORRECT_DATA_TYPE
        'bcq_file_invalid_date_format'           || INCORRECT_DATE_FORMAT
        'bcq_file_negative_bcq'                  || NEGATIVE_BCQ
        'bcq_file_invalid_bcq_length'            || INVALID_BCQ_LENGTH
        'bcq_file_invalid_bcq_fractional_length' || INVALID_BCQ_LENGTH
        'bcq_file_duplicate'                     || DUPLICATE_DATE
        'bcq_file_different_date'                || INVALID_TRADING_DATE
    }

    def readCsv(String csvFileName) {
        if (csvFileName == null) {
            return null
        }
        def csvPath = 'bcq/validation/csv/'.concat(csvFileName).concat('.csv');
        def file = new File(CsvValidationHelperTest.class.getClassLoader().getResource(csvPath).getFile())

        return reader.readCsv(new FileInputStream(file))
    }

}
