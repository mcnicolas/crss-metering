package com.pemc.crss.metering.validator.bcq.helper

import spock.lang.Specification
import spock.lang.Unroll

import static com.pemc.crss.metering.constants.BcqInterval.HOURLY
import static com.pemc.crss.metering.constants.BcqValidationError.CLOSED_TRADING_DATE
import static com.pemc.crss.metering.constants.BcqValidationError.DIFFERENT_TRADING_DATE
import static com.pemc.crss.metering.constants.BcqValidationError.INCOMPLETE_ENTRIES
import static com.pemc.crss.metering.constants.BcqValidationError.INCORRECT_TIME_INTERVALS
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
        def interval = HOURLY

        when:
        def headerList = populator.populate(readCsv(csv), interval)
        if (csv != 'bcq_file_closed_trading_date') {
            headerList.get(0).setTradingDate(startOfDay(now()))
        }
        def result = validationHelper.validHeaderList(declarationDateConfig, interval).test(headerList)

        then:
        result.status == REJECTED
        result.errorMessage.validationError == validationError

        where:
        csv                              | validationError
        'bcq_file_closed_trading_date'   | CLOSED_TRADING_DATE
        'bcq_file_invalid_data_size'     | INCOMPLETE_ENTRIES
        'bcq_file_invalid_time_interval' | INCORRECT_TIME_INTERVALS
    }

    @Unroll
    def 'settlement - must reject csv file with validation error: #validationError'() {
        given:
        def tradingDate = startOfDay(now())
        def interval = HOURLY

        when:
        def headerList = populator.populate(readCsv(csv), interval)
        if (csv != 'bcq_file_different_trading_date') {
            headerList.get(0).setTradingDate(startOfDay(now()))
        }
        def result = validationHelper.validHeaderList(tradingDate, interval).test(headerList)

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
        def interval = HOURLY

        when:
        def headerList = populator.populate(readCsv('bcq_file_valid'), interval)
        headerList.get(0).setTradingDate(startOfDay(now()))
        def result = validationHelper.validHeaderList(declarationDateConfig, interval).test(headerList)

        then:
        result.status == ACCEPTED
    }

    def 'settlement - must accept csv file'() {
        given:
        def tradingDate = startOfDay(now())
        def interval = HOURLY

        when:
        def headerList = populator.populate(readCsv('bcq_file_valid'), interval)
        headerList.get(0).setTradingDate(startOfDay(now()))
        def result = validationHelper.validHeaderList(tradingDate, interval).test(headerList)

        then:
        result.status == ACCEPTED
    }

}
