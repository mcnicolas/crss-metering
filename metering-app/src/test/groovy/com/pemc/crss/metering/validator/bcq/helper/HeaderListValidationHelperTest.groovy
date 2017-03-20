package com.pemc.crss.metering.validator.bcq.helper

import spock.lang.Specification
import spock.lang.Unroll

import static com.pemc.crss.metering.constants.BcqValidationError.*
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED
import static com.pemc.crss.metering.utils.DateTimeUtils.now
import static com.pemc.crss.metering.utils.DateTimeUtils.startOfDay
import static com.pemc.crss.metering.validator.bcq.helper.ValidationHelperTestUtils.readCsv

class HeaderListValidationHelperTest extends Specification {

    def validationHelper = new HeaderListValidationHelper()
    def populator = new BcqPopulator()

    @Unroll
    def 'must reject csv file with validation error: #validationError'() {
        given:
        def declarationDateConfig = 1

        when:
        def headerList = populator.populate(readCsv(csv))
        if (csv != 'bcq_file_closed_trading_date') {
            headerList.get(0).setTradingDate(startOfDay(now()))
        }
        def result = validationHelper.validHeaderList(declarationDateConfig).test(headerList)

        then:
        result.status == REJECTED
        result.errorMessage.validationError == validationError

        where:
        csv                              | validationError
        'bcq_file_invalid_data_size'     | INCOMPLETE_ENTRIES
        'bcq_file_invalid_time_interval' | INCORRECT_TIME_INTERVALS
        'bcq_file_closed_trading_date'   | CLOSED_TRADING_DATE
    }

    @Unroll
    def 'settlement - must reject csv file with validation error: #validationError'() {
        given:
        def tradingDate = startOfDay(now())

        when:
        def headerList = populator.populate(readCsv(csv))
        if (csv != 'bcq_file_different_trading_date') {
            headerList.get(0).setTradingDate(startOfDay(now()))
        }
        def result = validationHelper.validHeaderList(tradingDate).test(headerList)

        then:
        result.status == REJECTED
        result.errorMessage.validationError == validationError

        where:
        csv                                 | validationError
        'bcq_file_different_trading_date'   | DIFFERENT_TRADING_DATE
        'bcq_file_invalid_data_size'        | INCOMPLETE_ENTRIES
        'bcq_file_invalid_time_interval'    | INCORRECT_TIME_INTERVALS
    }

    def 'must accept csv file'() {
        given:
        def declarationDateConfig = 1

        when:
        def headerList = populator.populate(readCsv('bcq_file_valid'))
        headerList.get(0).setTradingDate(startOfDay(now()))
        def result = validationHelper.validHeaderList(declarationDateConfig).test(headerList)

        then:
        result.status == ACCEPTED
    }

    def 'settlement - must accept csv file'() {
        given:
        def tradingDate = startOfDay(now())

        when:
        def headerList = populator.populate(readCsv('bcq_file_valid'))
        headerList.get(0).setTradingDate(startOfDay(now()))
        def result = validationHelper.validHeaderList(tradingDate).test(headerList)

        then:
        result.status == ACCEPTED
    }

}
